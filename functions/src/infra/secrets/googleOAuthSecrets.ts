import { SecretManagerServiceClient } from "@google-cloud/secret-manager";
import { logger } from "firebase-functions";
import { RuntimeConfig } from "../../shared/runtimeConfig";
import { GoogleOAuthSecrets } from "../../type/GoogleOAuthSecrets";

const SECRET_NAME = "GOOGLE_OAUTH2";

export interface GoogleOAuthSecretProvider {
  /** The secrets. Throws when they can't be loaded. */
  load(): Promise<GoogleOAuthSecrets>;
}

/** Reads the secret's payload (JSON text). Replaced with a fake in tests. */
export type SecretFetcher = (name: string) => Promise<string>;

/**
 * Reads a secret from Secret Manager.
 * The client is created on the first call, not when this module is imported.
 */
export const createSecretManagerFetcher = (projectId: string | undefined): SecretFetcher => {
  let client: SecretManagerServiceClient | null = null;
  return async (name) => {
    client ??= new SecretManagerServiceClient();
    const [version] = await client.accessSecretVersion({
      name: `projects/${projectId}/secrets/${name}/versions/latest` /* latestじゃなくて4にしてもいいか。 */,
    });
    const data = version.payload?.data as Buffer | undefined;
    if (!data) throw new Error("Failed to load secret from Secret Manager.");
    return data.toString("utf8");
  };
};

/**
 * Loads the Google OAuth secrets.
 *
 * Secret Manager charges per access, so the secrets are loaded at most once per instance.
 * Calls that arrive while the first load is running share it. A failed load is not kept,
 * so the next call tries again.
 */
export const createGoogleOAuthSecretProvider = (
  config: RuntimeConfig,
  fetchSecret: SecretFetcher = createSecretManagerFetcher(config.projectId)
): GoogleOAuthSecretProvider => {
  let pending: Promise<GoogleOAuthSecrets> | null = null;

  const loadOnce = async (): Promise<GoogleOAuthSecrets> => {
    let json: string;
    if (config.isEmulator) {
      logger.log("Loading secrets from local environment variables (EMULATOR Mode)");
      if (!config.emulatorGoogleOAuthSecrets) {
        throw new Error(`Unable to find secrets in env`);
      }
      json = config.emulatorGoogleOAuthSecrets;
    } else {
      logger.log("Loading secrets from Secret Manager without using cache");
      json = await fetchSecret(SECRET_NAME);
    }

    let parsed: GoogleOAuthSecrets;
    try {
      parsed = JSON.parse(json);
    } catch {
      throw new Error("Failed to parse secret JSON.");
    }

    const { clientId, clientSecret, redirectUri, encryptionKey } = parsed;
    if (!clientId || !clientSecret || !redirectUri || !encryptionKey) {
      throw new Error("Incomplete secret fields.");
    }
    return parsed;
  };

  return {
    async load() {
      if (pending) {
        logger.log("Returning cached Google OAuth secrets.");
      } else {
        pending = loadOnce();
        /* Don't keep a failed load, so the next call retries. */
        pending.catch(() => {
          pending = null;
        });
      }

      return pending;
    },
  };
};
