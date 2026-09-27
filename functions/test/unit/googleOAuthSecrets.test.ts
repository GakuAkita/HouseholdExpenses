import { describe, expect, it, vi } from "vitest";
import { createGoogleOAuthSecretProvider } from "../../src/infra/secrets/googleOAuthSecrets";
import { RuntimeConfig } from "../../src/shared/runtimeConfig";

const secrets = {
  clientId: "client-id",
  clientSecret: "client-secret",
  redirectUri: "https://example.com/callback",
  encryptionKey: "key",
};

const production: RuntimeConfig = {
  isEmulator: false,
  projectId: "project",
  emulatorGmail: undefined,
  emulatorGoogleOAuthSecrets: undefined,
};

describe("createGoogleOAuthSecretProvider", () => {
  it("reads Secret Manager only once for many loads", async () => {
    const fetchSecret = vi.fn().mockResolvedValue(JSON.stringify(secrets));
    const provider = createGoogleOAuthSecretProvider(production, fetchSecret);

    /* Some loads start before the first one finishes. */
    const results = await Promise.all([provider.load(), provider.load(), provider.load()]);
    await provider.load();

    expect(fetchSecret).toHaveBeenCalledTimes(1);
    expect(fetchSecret).toHaveBeenCalledWith("GOOGLE_OAUTH2");
    for (const result of results) {
      expect(result).toEqual(secrets);
    }
  });

  it("tries again after a failed load", async () => {
    const fetchSecret = vi
      .fn()
      .mockRejectedValueOnce(new Error("PERMISSION_DENIED"))
      .mockResolvedValue(JSON.stringify(secrets));
    const provider = createGoogleOAuthSecretProvider(production, fetchSecret);

    await expect(provider.load()).rejects.toThrow("PERMISSION_DENIED");
    expect(await provider.load()).toEqual(secrets);
    expect(fetchSecret).toHaveBeenCalledTimes(2);
  });

  it("fails when a field is missing", async () => {
    const { encryptionKey: _, ...incomplete } = secrets;
    const provider = createGoogleOAuthSecretProvider(
      production,
      vi.fn().mockResolvedValue(JSON.stringify(incomplete))
    );
    await expect(provider.load()).rejects.toThrow("Incomplete secret fields.");
  });

  it("reads the environment variable in the emulator, not Secret Manager", async () => {
    const fetchSecret = vi.fn();
    const provider = createGoogleOAuthSecretProvider(
      { ...production, isEmulator: true, emulatorGoogleOAuthSecrets: JSON.stringify(secrets) },
      fetchSecret
    );
    expect(await provider.load()).toEqual(secrets);
    expect(fetchSecret).not.toHaveBeenCalled();
  });
});
