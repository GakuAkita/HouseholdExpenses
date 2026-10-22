import { describe, expect, it } from "vitest";
import { GmailApiClient } from "../../src/infra/gmail/GmailApiClient";
import { RakutenCardAllParser } from "../../src/mail/parsers/RakutenCardAllParser";
import { RakutenPayMailParser } from "../../src/mail/parsers/RakutenPayMailParser";
import { decryptWithKey } from "../../src/shared/encryption";
import { extractHtmlBody, extractTextBody } from "../../src/infra/gmail/extractHtmlBody";
import { getRakutenCardAllMailIds, getRakutenPayMailIds } from "../../src/infra/gmail/mailQueries";
import { GoogleOAuthSecrets } from "../../src/type/GoogleOAuthSecrets";

/*
 * Calls the real Gmail API with your refresh token. Read-only.
 * Needs GOOGLE_OAUTH_SECRETS and ENCRYPTED_REFRESH_TOKEN in ../.env.local; skipped otherwise.
 *   npm run test:live
 */
const secretsJson = process.env.GOOGLE_OAUTH_SECRETS;
const encryptedRefreshToken = process.env.ENCRYPTED_REFRESH_TOKEN;

describe.skipIf(!secretsJson || !encryptedRefreshToken)("Gmail (live)", () => {
  const createClient = () => {
    const secrets: GoogleOAuthSecrets = JSON.parse(secretsJson!);
    return new GmailApiClient({
      clientId: secrets.clientId,
      clientSecret: secrets.clientSecret,
      refreshToken: decryptWithKey(encryptedRefreshToken!, secrets.encryptionKey),
    });
  };

  it("authorizes with the stored refresh token", async () => {
    /* Throws when Google rejects the refresh token. */
    expect(await createClient().authorize()).toBeTruthy();
  });

  it("finds Rakuten Pay mails and parses the latest one", async () => {
    const client = createClient();
    const oneYearAgo = Math.floor(Date.now() / 1000) - 365 * 24 * 60 * 60;
    const ids = await getRakutenPayMailIds(client, oneYearAgo, Math.floor(Date.now() / 1000));
    if (ids.length === 0) return;

    const detail = await client.getMessageDetail(ids[0]);
    const text = extractTextBody(detail.payload);
    expect(text).toBeTruthy();
    /* Checks the real mail format still matches the parser (throws otherwise), without printing the mail. */
    expect(new RakutenPayMailParser(text!).toExpense().amount).toBeTypeOf("number");
  });

  it("finds Rakuten Card mails and parses the latest one", async () => {
    const client = createClient();
    const oneYearAgo = Math.floor(Date.now() / 1000) - 365 * 24 * 60 * 60;
    const ids = await getRakutenCardAllMailIds(client, oneYearAgo, Math.floor(Date.now() / 1000));
    if (ids.length === 0) return;

    const detail = await client.getMessageDetail(ids[0]);
    /* Same as the job: an HTML-only mail is read as HTML with the tags removed. */
    const text = extractTextBody(detail.payload) ?? extractHtmlBody(detail.payload, true);
    expect(text).toBeTruthy();
    /* Throws when the real format no longer matches. A mail with only 楽天ペイ charges returns []. */
    expect(Array.isArray(new RakutenCardAllParser(text!).toExpenses())).toBe(true);
  });
});
