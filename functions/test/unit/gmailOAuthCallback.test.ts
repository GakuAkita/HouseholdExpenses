import { describe, expect, it, vi } from "vitest";
import { GmailOAuthCallbackDeps, handleGmailOAuthCallback } from "../../src/jobs/gmailOAuthCallback";
import { GoogleOAuthApi } from "../../src/infra/gmail/GoogleOAuthApi";
import { FuncStatus } from "../../src/type/FuncStatus";

const secrets = {
  clientId: "client-id",
  clientSecret: "client-secret",
  redirectUri: "https://example.com/callback",
  encryptionKey: "encryption-key",
};

const setUp = (overrides: {
  googleOAuthApi?: Partial<GoogleOAuthApi>;
  userEmail?: string;
  verifyIdToken?: () => Promise<unknown>;
} = {}) => {
  const saveToken = vi.fn().mockResolvedValue({ status: FuncStatus.SUCCESS });
  const exchangeCode = vi.fn().mockResolvedValue({ accessToken: "access", refreshToken: "refresh" });
  const deps = {
    auth: {
      verifyIdToken: overrides.verifyIdToken ?? vi.fn().mockResolvedValue({ uid: "user1" }),
      getUser: vi.fn().mockResolvedValue({ email: overrides.userEmail ?? "user@gmail.com" }),
    },
    runtime: {
      secrets: { load: async () => ({ status: FuncStatus.SUCCESS, data: secrets }) },
      googleOAuthApi: {
        exchangeCode,
        fetchEmail: vi.fn().mockResolvedValue("user@gmail.com"),
        ...overrides.googleOAuthApi,
      },
    },
    mailboxExtractionService: { setMailboxExtractionTokenWithEncryption: saveToken },
  } as unknown as GmailOAuthCallbackDeps;
  return { deps, saveToken, exchangeCode };
};

describe("handleGmailOAuthCallback", () => {
  it("saves the refresh token with the encryption key", async () => {
    const { deps, saveToken, exchangeCode } = setUp();

    expect(await handleGmailOAuthCallback(deps, { state: "id-token", code: "auth-code" })).toBe("connected");

    expect(exchangeCode).toHaveBeenCalledWith("auth-code", secrets);
    expect(saveToken).toHaveBeenCalledWith(
      "user1",
      { refreshToken: "refresh", gmail: "user@gmail.com" },
      "encryption-key"
    );
  });

  it("rejects a Gmail account that isn't the user's sign-in address", async () => {
    const { deps, saveToken } = setUp({ userEmail: "someone.else@gmail.com" });

    expect(await handleGmailOAuthCallback(deps, { state: "id-token", code: "auth-code" })).toBe("failed");
    expect(saveToken).not.toHaveBeenCalled();
  });

  it("fails without a refresh token", async () => {
    const { deps, saveToken } = setUp({
      googleOAuthApi: { exchangeCode: vi.fn().mockResolvedValue({ accessToken: "access" }) },
    });

    expect(await handleGmailOAuthCallback(deps, { state: "id-token", code: "auth-code" })).toBe("failed");
    expect(saveToken).not.toHaveBeenCalled();
  });

  it("fails for an invalid ID token", async () => {
    const { deps, exchangeCode } = setUp({
      verifyIdToken: () => Promise.reject(new Error("auth/argument-error")),
    });

    expect(await handleGmailOAuthCallback(deps, { state: "bad", code: "auth-code" })).toBe("failed");
    expect(exchangeCode).not.toHaveBeenCalled();
  });

  it("reports missing parameters", async () => {
    const { deps } = setUp();
    expect(await handleGmailOAuthCallback(deps, { state: undefined, code: "auth-code" })).toBe(
      "missing_parameters"
    );
    expect(await handleGmailOAuthCallback(deps, { state: "id-token", code: ["a", "b"] })).toBe(
      "missing_parameters"
    );
  });
});
