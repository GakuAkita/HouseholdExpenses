import * as admin from "firebase-admin";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { handleGmailOAuthCallback } from "../../src/jobs/gmailOAuthCallback";
import { GoogleOAuthApi } from "../../src/infra/gmail/GoogleOAuthApi";
import { createTestServices, deleteTestUser, newTestUserId, testGmailOf, testSecrets } from "./emulator";

describe("Gmail OAuth callback", () => {
  const userId = newTestUserId();
  const gmail = testGmailOf(userId);
  const nonce = "n".repeat(43);
  const requestRef = () => admin.database().ref(`users/${userId}/mailbox_extraction/gmail_oauth_request`);

  const verifiersUsed: string[] = [];
  const fakeGoogle: GoogleOAuthApi = {
    exchangeCode: async (code, codeVerifier) => {
      verifiersUsed.push(codeVerifier);
      return { accessToken: `access-for-${code}`, refreshToken: "raw-refresh-token" };
    },
    fetchEmail: async () => gmail,
    revokeToken: async () => "revoked",
  };
  const services = createTestServices({ googleOAuthApi: fakeGoogle });

  /** What the app writes before it opens Google's consent page. */
  const writeRequest = (createdAt: number | object = admin.database.ServerValue.TIMESTAMP) =>
    requestRef().set({ nonce, codeVerifier: "v".repeat(43), createdAt });

  beforeEach(async () => {
    verifiersUsed.length = 0;
    await admin.auth().createUser({ uid: userId, email: gmail });
  });
  afterEach(() => deleteTestUser(userId));

  it("saves the encrypted refresh token, using the PKCE verifier, and removes the request", async () => {
    await writeRequest();

    const result = await handleGmailOAuthCallback(services, { state: `${userId}.${nonce}`, code: "auth-code" });

    expect(result).toBe("connected");
    expect(verifiersUsed).toEqual(["v".repeat(43)]);
    const token = await services.mailboxExtractionService.getGmailToken(userId, testSecrets.encryptionKey);
    expect(token).toMatchObject({ refreshToken: "raw-refresh-token", gmail });
    expect((await requestRef().get()).exists()).toBe(false);
  });

  it("accepts a state only once", async () => {
    await writeRequest();
    const params = { state: `${userId}.${nonce}`, code: "auth-code" };

    expect(await handleGmailOAuthCallback(services, params)).toBe("connected");
    expect(await handleGmailOAuthCallback(services, params)).toBe("failed");
  });

  it("fails for a wrong nonce and keeps the request for the real callback", async () => {
    await writeRequest();

    expect(
      await handleGmailOAuthCallback(services, { state: `${userId}.${"x".repeat(43)}`, code: "auth-code" })
    ).toBe("failed");
    expect(verifiersUsed).toEqual([]);
    expect((await requestRef().get()).exists()).toBe(true);
  });

  it("fails when the app didn't write a request", async () => {
    expect(await handleGmailOAuthCallback(services, { state: `${userId}.${nonce}`, code: "auth-code" })).toBe(
      "failed"
    );
    expect(verifiersUsed).toEqual([]);
  });

  it("fails for an expired request", async () => {
    await writeRequest(Date.now() - 11 * 60 * 1000);

    expect(await handleGmailOAuthCallback(services, { state: `${userId}.${nonce}`, code: "auth-code" })).toBe(
      "failed"
    );
    expect(verifiersUsed).toEqual([]);
  });

  it("fails for a Firebase ID token as state (the old format)", async () => {
    expect(await handleGmailOAuthCallback(services, { state: "not-a-token", code: "auth-code" })).toBe("failed");
  });
});
