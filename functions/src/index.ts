import { logger } from "firebase-functions";
import { onSchedule } from "firebase-functions/scheduler";
import * as functions from "firebase-functions/v1";
import { TriggerTimeZone } from "./constants/TimeZone";
import { handleGmailOAuthCallback } from "./jobs/gmailOAuthCallback";
import {
  runAmazonSubscribeMonitorJob,
  runMailboxExtractionJob,
  runRepeatAddJob,
} from "./jobs/scheduledJobs";
import { initializeServices, Services } from "./myFunc/initializeServices";
import { mailboxExtractionSchedules } from "./type/Mailbox";

/*
 * Only the trigger definitions. The work is done in src/jobs, which tests call directly.
 * The exported names must not change: a renamed export is deployed as a new function
 * and the old one is deleted.
 */

/**
 * Services are created on the first call, not when this module is loaded.
 * Deploying and importing the module (e.g. in tests) doesn't touch Firebase.
 */
let services: Services | null = null;
const getServices = (): Services => (services ??= initializeServices());

exports.monthly_repeatAddJob = onSchedule(
  {
    schedule: "0 1 1 * *", // 毎月1日 1:00 JST
    timeZone: TriggerTimeZone, // 現在時刻の設定も日本にしているから、大丈夫。
    concurrency: 1,
  },
  async (_) => {
    logger.log("Starting monthly repeatAdd job...");
    await runRepeatAddJob(getServices());
  },
);

/**
 * ユーザーが作成されたときに走らせる
 * 注意：Node.js 18は2025-10-30に廃止されたため、Node.js 20以上が必須。2025/11/2
 */
exports.onUserCreate = functions.auth.user().onCreate(async (user) => {
  const uid = user.uid;
  const email = user.email;

  logger.log(`New user created! id:${uid} email:${email}`);

  if (email == undefined) {
    logger.error("Unable to get Email..");
  } else {
    /* Awaited, so the function doesn't end before the settings are written. */
    await getServices().userSettingsProcessor.setInitialUserSettings(uid, email);
  }
});

/**
 * Gmailアクセスの許可を取得したときの処理
 */
exports.handleOAuthCallback = functions.https.onRequest(async (req, res) => {
  const result = await handleGmailOAuthCallback(getServices(), {
    state: req.query.state,
    code: req.query.code,
  });

  switch (result) {
    case "connected":
      res.send(
        `<h1>I'm God Akita.</h1><h2>Process finished.<br>Please close this window.</h2>`,
      );
      return;
    case "failed":
      res.status(200).send(`OAuth token exchange failed.`);
      return;
    case "missing_parameters":
      /* Current behaviour: no response is sent. */
      return;
  }
});

for (const [_, schedule] of mailboxExtractionSchedules.entries()) {
  /* この関数はあくまでスケジュールをdeployしているだけ */
  exports[`mailboxExtractionJob_${schedule.id}`] = onSchedule(
    {
      schedule: schedule.cron,
      timeZone: TriggerTimeZone,
      concurrency: 1,
    },
    async () => {
      await runMailboxExtractionJob(getServices(), schedule.mailTypes);

      if (schedule.id === "daily") {
        /**
         * CloudSchedulerは合計3つしか無料で使えないらしい
         * RepeatAddで毎月実行は必要だから、他のはdailyとshortPeriodの2つしか使えない。
         * したがって、なにか増えたときはこのようにidで条件分岐をして加えていく
         */
        await runAmazonSubscribeMonitorJob(getServices());
      }
    },
  );
}
