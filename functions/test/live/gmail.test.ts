import { describe, expect, it } from "vitest";
import { GmailApiClient } from "../../src/infra/gmail/GmailApiClient";
import { RakutenPayMailParser } from "../../src/mail/parsers/RakutenPayMailParser";
import { decryptWithKey } from "../../src/shared/encryption";
import { extractTextBody } from "../../src/infra/gmail/extractHtmlBody";
import { getRakutenPayMailIds } from "../../src/infra/gmail/mailQueries";
import { FuncStatus } from "../../src/type/FuncStatus";
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
    const result = await createClient().authorize();
    expect(result.status, result.message).toBe(FuncStatus.SUCCESS);
  });

  it("finds Rakuten Pay mails and parses the latest one", async () => {
    const client = createClient();
    const oneYearAgo = Math.floor(Date.now() / 1000) - 365 * 24 * 60 * 60;
    const ids = await getRakutenPayMailIds(client, oneYearAgo, Math.floor(Date.now() / 1000));
    expect(ids.status, ids.message).toBe(FuncStatus.SUCCESS);
    if (!ids.data?.length) return;

    const detail = await client.getMessageDetail(ids.data[0]);
    const text = extractTextBody(detail.data?.payload);
    expect(text).toBeTruthy();
    const parsed = new RakutenPayMailParser(text!).toExpense();
    /* Checks the real mail format still matches the parser, without printing the mail. */
    expect(parsed.status, parsed.message).toBe(FuncStatus.SUCCESS);
  });
});
