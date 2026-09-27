import * as admin from "firebase-admin";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { handleGmailOAuthCallback } from "../../src/jobs/gmailOAuthCallback";
import { GoogleOAuthApi } from "../../src/myFunc/Client/GoogleOAuthApi";
import { FuncStatus } from "../../src/type/FuncStatus";
import { createTestServices, deleteTestUser, newTestUserId, testGmailOf, testSecrets } from "./emulator";

/** Signs in to the Auth emulator and returns the user's Firebase ID token, like the app does. */
const idTokenOf = async (userId: string): Promise<string> => {
  const customToken = await admin.auth().createCustomToken(userId);
  const response = await fetch(
    `http://${process.env.FIREBASE_AUTH_EMULATOR_HOST}/identitytoolkit.googleapis.com/v1/accounts:signInWithCustomToken?key=fake-api-key`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ token: customToken, returnSecureToken: true }),
    }
  );
  const body = (await response.json()) as { idToken: string };
  return body.idToken;
};

describe("Gmail OAuth callback", () => {
  const userId = newTestUserId();
  const gmail = testGmailOf(userId);
  const fakeGoogle: GoogleOAuthApi = {
    exchangeCode: async (code) => ({ accessToken: `access-for-${code}`, refreshToken: "raw-refresh-token" }),
    fetchEmail: async () => gmail,
  };
  const services = createTestServices({ googleOAuthApi: fakeGoogle });

  beforeEach(() => admin.auth().createUser({ uid: userId, email: gmail }));
  afterEach(() => deleteTestUser(userId));

  it("saves the encrypted refresh token for the signed-in user", async () => {
    const result = await handleGmailOAuthCallback(services, {
      state: await idTokenOf(userId),
      code: "auth-code",
    });

    expect(result).toBe("connected");
    const token = await services.mailboxExtractionService.getMailboxExtractionGmailTokenWithDecryption(
      userId,
      testSecrets.encryptionKey
    );
    expect(token).toMatchObject({
      status: FuncStatus.SUCCESS,
      data: { refreshToken: "raw-refresh-token", gmail },
    });
  });

  it("fails for a state that isn't a Firebase ID token", async () => {
    expect(await handleGmailOAuthCallback(services, { state: "not-a-token", code: "auth-code" })).toBe("failed");
  });
});
