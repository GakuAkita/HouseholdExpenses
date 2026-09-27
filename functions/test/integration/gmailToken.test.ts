import * as admin from "firebase-admin";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { FuncStatus } from "../../src/type/FuncStatus";
import { createTestServices, deleteTestUser, newTestUserId, testGmailOf, testSecrets } from "./emulator";

describe("Gmail refresh token", () => {
  const userId = newTestUserId();
  const gmail = testGmailOf(userId);
  const services = createTestServices();

  /* The token is stored under the user's sign-in email, which comes from Firebase Auth. */
  beforeEach(() => admin.auth().createUser({ uid: userId, email: gmail }));
  afterEach(() => deleteTestUser(userId));

  it("is stored encrypted and read back decrypted", async () => {
    const saved = await services.mailboxExtractionService.setMailboxExtractionTokenWithEncryption(
      userId,
      { refreshToken: "raw-refresh-token", gmail },
      testSecrets.encryptionKey
    );
    expect(saved.status).toBe(FuncStatus.SUCCESS);

    const stored = (await admin.database().ref(`users/${userId}/mailbox_extraction/gmail_tokens`).get()).val();
    const [storedToken] = Object.values(stored) as { refreshToken: string; gmail: string; timestamp: string }[];
    expect(storedToken.gmail).toBe(gmail);
    expect(storedToken.refreshToken).not.toContain("raw-refresh-token");
    expect(storedToken.refreshToken).toMatch(/^[0-9a-f]{32}:[0-9a-f]+$/);

    const read = await services.mailboxExtractionService.getMailboxExtractionGmailTokenWithDecryption(
      userId,
      testSecrets.encryptionKey
    );
    expect(read).toMatchObject({
      status: FuncStatus.SUCCESS,
      data: { refreshToken: "raw-refresh-token", gmail },
    });
  });

  it("reports EMPTY when the user hasn't connected Gmail", async () => {
    const read = await services.mailboxExtractionService.getMailboxExtractionGmailTokenWithDecryption(
      userId,
      testSecrets.encryptionKey
    );
    expect(read.status).toBe(FuncStatus.EMPTY);
  });
});
