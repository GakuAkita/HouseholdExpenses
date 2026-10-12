/*
 * Runs a mail job's Gmail query against your real Gmail and parses what it finds, without
 * writing anything: no expenses, no subscribe list changes, no last_exec. Use it to see what a
 * job would pick up, or to check a parser against real mails.
 *
 *   npm run gmail:dryrun -- list                          the jobs you can run
 *   npm run gmail:dryrun -- <job>                         one job (e.g. rakuten_pay, amazon_subscribe_monitor)
 *   npm run gmail:dryrun -- all                           every job
 *   npm run gmail:dryrun -- query "<gmail query>"         any Gmail query: lists the mails it finds
 *
 * Options:
 *   --days N     search the last N days (default 30). The real jobs search since their last run.
 *   --max N      query mode only: how many mails to list (default 20)
 *   --text       print the text each parser receives
 *   --verbose    show the functions' own logs
 *
 * Needs GOOGLE_OAUTH_SECRETS and ENCRYPTED_REFRESH_TOKEN in ../.env.local, like npm run test:live.
 * Read-only: it only searches and reads mails. It uses the compiled code in lib/ (the npm script builds it first).
 */
const path = require("path");
require("dotenv").config({ path: path.resolve(__dirname, "../../.env.local") });

const args = process.argv.slice(2);
const flag = (name) => args.includes(`--${name}`);
const option = (name, fallback) => {
  const index = args.indexOf(`--${name}`);
  return index >= 0 ? Number(args[index + 1]) : fallback;
};
const positional = args.filter((arg, i) => !arg.startsWith("--") && !args[i - 1]?.match(/^--(days|max)$/));

/*
 * The functions log a lot. Hide it unless --verbose, and keep it short when shown.
 * firebase-functions' logger keeps the console methods it finds when it is loaded,
 * so replace them while it loads and put them back afterwards.
 */
const levels = ["debug", "info", "log", "warn", "error"];
const originalConsole = Object.fromEntries(levels.map((level) => [level, console[level]]));
for (const level of levels) {
  console[level] = flag("verbose")
    ? (json) => originalConsole.log(`      [log] ${JSON.parse(json).message}`)
    : () => {};
}
require("firebase-functions/logger");
Object.assign(console, originalConsole);

const { GmailApiClient } = require("../lib/infra/gmail/GmailApiClient");
const { decryptWithKey } = require("../lib/shared/encryption");
const { extractHtmlBody, extractTextBody, getSubjectFromMessage } = require("../lib/infra/gmail/extractHtmlBody");
const { getAmazonSubscribeNextShipNotifyAndCancelMailIds } = require("../lib/infra/gmail/mailQueries");
const { readSubscribeMail } = require("../lib/jobs/AmazonSubscribeMonitorItemsProcessor");
const { AmazonItemDispatchedMailParser } = require("../lib/mail/parsers/AmazonItemDispatchedMailParser");
const { mailSourceFor } = require("../lib/mail/sources");
const { mailboxExtractionSchedules } = require("../lib/type/Mailbox");

const MONITOR = "amazon_subscribe_monitor";

/** Every job: the mail types of each schedule, and the subscribe monitor (runs with "daily"). */
const jobs = [
  ...mailboxExtractionSchedules.flatMap((schedule) =>
    schedule.mailTypes.map((setting) => ({ name: setting.nodeName, schedule: schedule.id, setting }))
  ),
  { name: MONITOR, schedule: "daily" },
];

const createClient = () => {
  const secretsJson = process.env.GOOGLE_OAUTH_SECRETS;
  const encryptedRefreshToken = process.env.ENCRYPTED_REFRESH_TOKEN;
  if (!secretsJson || !encryptedRefreshToken) {
    throw new Error("Set GOOGLE_OAUTH_SECRETS and ENCRYPTED_REFRESH_TOKEN in .env.local (the repository root).");
  }
  const secrets = JSON.parse(secretsJson);
  return new GmailApiClient({
    clientId: secrets.clientId,
    clientSecret: secrets.clientSecret,
    refreshToken: decryptWithKey(encryptedRefreshToken, secrets.encryptionKey),
  });
};

const jst = (internalDate) =>
  internalDate ? new Date(Number(internalDate)).toLocaleString("ja-JP", { timeZone: "Asia/Tokyo" }) : "(no date)";

const printMailHeader = (id, message) =>
  console.log(`\n  ${jst(message.internalDate)}  ${getSubjectFromMessage(message) ?? "(no subject)"}  [${id}]`);

const printText = (label, text) => {
  if (!flag("text")) return;
  console.log(`    --- ${label} ---`);
  console.log((text ?? "(none)").replace(/^/gm, "    | "));
};

const describeExpense = (e) =>
  [e.datetime, `${e.amount}円`, e.storeName && `store=${e.storeName}`, e.itemName && `item=${e.itemName}`, e.note && `note=${e.note}`]
    .filter(Boolean)
    .join("  ");

/** Fetches the mails, newest first. */
const fetchMails = async (client, ids) => {
  const mails = [];
  for (const id of ids) mails.push([id, await client.getMessageDetail(id)]);
  return mails.sort(([, a], [, b]) => Number(b.internalDate ?? 0) - Number(a.internalDate ?? 0));
};

/** A context without the user's data: no categories, no assignment rules, an empty subscribe list. */
const emptyContext = {
  categories: {},
  categoryAssignmentData: { storeName: {}, productName: {} },
  loadEnabledAmazonSubscribeItems: async () => ({}),
};

const runSourceJob = async (client, job, after, before) => {
  const source = mailSourceFor(job.setting);
  const ids = await source.findMailIds(client, after, before);
  console.log(`  Found ${ids.length} mail(s). The job reads at most this many per run, so a full page means some may be left out.`);

  for (const [id, message] of await fetchMails(client, ids)) {
    printMailHeader(id, message);
    const rawText = extractTextBody(message.payload);
    printText("text/plain body", rawText);
    if (!rawText) {
      console.log("    ✗ No text/plain body. The job skips this mail.");
      continue;
    }
    try {
      if (job.name === "amazon_subscribe") {
        /* The real source keeps only products in the user's subscribe list, which isn't loaded here. */
        const dispatched = new AmazonItemDispatchedMailParser(rawText, message.internalDate).toExpenses();
        console.log(`    ✓ ${dispatched.length} dispatched product(s). Saved only if the name matches the subscribe list:`);
        for (const e of dispatched) console.log(`      - ${describeExpense(e)}`);
        continue;
      }
      const expenses = await source.toExpenses({ rawText, internalDate: message.internalDate }, job.setting, emptyContext);
      console.log(`    ✓ ${expenses.length} expense(s)`);
      for (const e of expenses) console.log(`      - ${describeExpense(e)}`);
    } catch (error) {
      console.log(`    ✗ ${error.name}: ${error.message}`);
    }
  }
};

const runMonitorJob = async (client, after, before) => {
  const ids = await getAmazonSubscribeNextShipNotifyAndCancelMailIds(client, after, before, 10);
  console.log(`  Found ${ids.length} mail(s). The job reads at most 10 per run, so 10 means some may be left out.`);

  for (const [id, message] of await fetchMails(client, ids)) {
    printMailHeader(id, message);
    printText("HTML body without tags", extractHtmlBody(message.payload, true));
    printText("text/plain body", extractTextBody(message.payload));
    try {
      const action = readSubscribeMail(message);
      if (action.kind === "upsert") {
        console.log(`    ✓ add or update ${action.items.length} item(s):`);
        for (const item of action.items) {
          console.log(`      - ${item.productName}  ${item.price}円 × ${item.quantity}`);
        }
      } else if (action.kind === "remove") {
        console.log(`    ✓ remove: ${action.productName}`);
      } else {
        console.log(`    - ignored: ${action.reason}`);
      }
    } catch (error) {
      console.log(`    ✗ ${error.name}: ${error.message}`);
    }
  }
};

const runQuery = async (client, query, max) => {
  const ids = await client.queryMessages(query, max);
  console.log(`Query: ${query}\nFound ${ids.length} mail(s) (at most ${max}).`);
  for (const [id, message] of await fetchMails(client, ids)) {
    printMailHeader(id, message);
    const from = message.payload?.headers?.find((h) => h.name?.toLowerCase() === "from")?.value;
    console.log(`    from: ${from}`);
    printText("text/plain body", extractTextBody(message.payload));
  }
};

const main = async () => {
  const [command, query] = positional;
  if (!command || command === "list") {
    console.log("Jobs:");
    for (const job of jobs) console.log(`  ${job.name.padEnd(26)} (${job.schedule})`);
    console.log('\nUsage: npm run gmail:dryrun -- <job|all|list> [--days N] [--text] [--verbose]');
    console.log('       npm run gmail:dryrun -- query "<gmail query>" [--max N] [--text]');
    return;
  }

  const client = createClient();
  if (command === "query") {
    if (!query) throw new Error('Give the query: npm run gmail:dryrun -- query "from:example.com"');
    await runQuery(client, query, option("max", 20));
    return;
  }

  const selected = command === "all" ? jobs : jobs.filter((job) => job.name === command);
  if (selected.length === 0) {
    throw new Error(`Unknown job "${command}". Run: npm run gmail:dryrun -- list`);
  }

  const days = option("days", 30);
  const before = Math.floor(Date.now() / 1000);
  const after = before - days * 24 * 60 * 60;
  for (const job of selected) {
    console.log(`\n== ${job.name} (${job.schedule}), last ${days} day(s) ==`);
    try {
      if (job.name === MONITOR) await runMonitorJob(client, after, before);
      else await runSourceJob(client, job, after, before);
    } catch (error) {
      console.log(`  ✗ ${error.message}`);
    }
  }
};

main().catch((error) => {
  console.error(error.message ?? error);
  process.exit(1);
});
