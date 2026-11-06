import { describe, expect, it, vi } from "vitest";
import {
  GMAIL_OAUTH_REQUEST_TTL_MS,
  GmailOAuthCallbackDeps,
  handleGmailOAuthCallback,
  parseGmailOAuthState,
} from "../../src/jobs/gmailOAuthCallback";
import { GoogleOAuthApi } from "../../src/infra/gmail/GoogleOAuthApi";
import { fixedClock } from "../../src/shared/clock";
import { GmailOAuthRequest } from "../../src/type/Mailbox";

const secrets = {
  clientId: "client-id",
  clientSecret: "client-secret",
  redirectUri: "https://example.com/callback",
  encryptionKey: "encryption-key",
};

const now = new Date("2026-01-01T00:00:00Z");
const request: GmailOAuthRequest = {
  nonce: "nonce1",
  codeVerifier: "verifier1",
  createdAt: now.getTime() - 60_000,
};

const setUp = (overrides: {
  googleOAuthApi?: Partial<GoogleOAuthApi>;
  userEmail?: string;
  request?: GmailOAuthRequest | null;
} = {}) => {
  const saveToken = vi.fn().mockResolvedValue(undefined);
  const exchangeCode = vi.fn().mockResolvedValue({ accessToken: "access", refreshToken: "refresh" });
  const takeRequest = vi.fn().mockResolvedValue(overrides.request === undefined ? request : overrides.request);
  const deps = {
    auth: {
      getUser: vi.fn().mockResolvedValue({ email: overrides.userEmail ?? "user@gmail.com" }),
    },
    runtime: {
      clock: fixedClock(now),
      secrets: { load: async () => secrets },
      googleOAuthApi: {
        exchangeCode,
        fetchEmail: vi.fn().mockResolvedValue("user@gmail.com"),
        ...overrides.googleOAuthApi,
      },
    },
    mailboxExtractionService: { saveGmailToken: saveToken, takeGmailOAuthRequest: takeRequest },
  } as unknown as GmailOAuthCallbackDeps;
  return { deps, saveToken, exchangeCode, takeRequest };
};

describe("parseGmailOAuthState", () => {
  it("splits uid and nonce at the last dot", () => {
    expect(parseGmailOAuthState("user1.abc_-DEF")).toEqual({ uid: "user1", nonce: "abc_-DEF" });
  });

  it.each(["", "no-dot", ".nonce", "user1."])("rejects %j", (state) => {
    expect(parseGmailOAuthState(state)).toBeNull();
  });
});

describe("handleGmailOAuthCallback", () => {
  it("takes the request, exchanges the code with the PKCE verifier and saves the token", async () => {
    const { deps, saveToken, exchangeCode, takeRequest } = setUp();

    expect(await handleGmailOAuthCallback(deps, { state: "user1.nonce1", code: "auth-code" })).toBe("connected");

    expect(takeRequest).toHaveBeenCalledWith("user1", "nonce1");
    expect(exchangeCode).toHaveBeenCalledWith("auth-code", "verifier1", secrets);
    expect(saveToken).toHaveBeenCalledWith(
      "user1",
      { refreshToken: "refresh", gmail: "user@gmail.com" },
      "encryption-key"
    );
  });

  it("fails when no request matches the state (wrong nonce, already used, or never written)", async () => {
    const { deps, exchangeCode, saveToken } = setUp({ request: null });

    expect(await handleGmailOAuthCallback(deps, { state: "user1.wrong", code: "auth-code" })).toBe("failed");
    expect(exchangeCode).not.toHaveBeenCalled();
    expect(saveToken).not.toHaveBeenCalled();
  });

  it("fails for an expired request", async () => {
    const { deps, exchangeCode } = setUp({
      request: { ...request, createdAt: now.getTime() - GMAIL_OAUTH_REQUEST_TTL_MS - 1 },
    });

    expect(await handleGmailOAuthCallback(deps, { state: "user1.nonce1", code: "auth-code" })).toBe("failed");
    expect(exchangeCode).not.toHaveBeenCalled();
  });

  it("fails for a request dated in the future", async () => {
    const { deps, exchangeCode } = setUp({ request: { ...request, createdAt: now.getTime() + 60_000 } });

    expect(await handleGmailOAuthCallback(deps, { state: "user1.nonce1", code: "auth-code" })).toBe("failed");
    expect(exchangeCode).not.toHaveBeenCalled();
  });

  it("fails for a state that isn't <uid>.<nonce>, without looking anything up", async () => {
    const { deps, takeRequest } = setUp();

    expect(await handleGmailOAuthCallback(deps, { state: "a-firebase-id-token", code: "auth-code" })).toBe(
      "failed"
    );
    expect(takeRequest).not.toHaveBeenCalled();
  });

  it("rejects a Gmail account that isn't the user's sign-in address", async () => {
    const { deps, saveToken } = setUp({ userEmail: "someone.else@gmail.com" });

    expect(await handleGmailOAuthCallback(deps, { state: "user1.nonce1", code: "auth-code" })).toBe("failed");
    expect(saveToken).not.toHaveBeenCalled();
  });

  it("fails without a refresh token", async () => {
    const { deps, saveToken } = setUp({
      googleOAuthApi: { exchangeCode: vi.fn().mockResolvedValue({ accessToken: "access" }) },
    });

    expect(await handleGmailOAuthCallback(deps, { state: "user1.nonce1", code: "auth-code" })).toBe("failed");
    expect(saveToken).not.toHaveBeenCalled();
  });

  it("reports missing parameters", async () => {
    const { deps } = setUp();
    expect(await handleGmailOAuthCallback(deps, { state: undefined, code: "auth-code" })).toBe(
      "missing_parameters"
    );
    expect(await handleGmailOAuthCallback(deps, { state: "user1.nonce1", code: ["a", "b"] })).toBe(
      "missing_parameters"
    );
  });
});
