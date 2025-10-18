// myFunc/secrets/googleOAuthSecrets.ts

import { SecretManagerServiceClient } from "@google-cloud/secret-manager";
import { logger } from "firebase-functions";
import { FuncResultWithData, FuncStatus } from "../type/FuncStatus";
import { GoogleOAuthSecrets } from "../type/GoogleOAuthSecrets";

const secretClient = new SecretManagerServiceClient();
let cachedGoogleOAuthSecrets: GoogleOAuthSecrets | null = null;

export const loadGoogleOAuthSecrets = async (): Promise<
  FuncResultWithData<GoogleOAuthSecrets>
> => {
  if (cachedGoogleOAuthSecrets) {
    logger.log("Returning cached Google OAuth secrets.");
    return {
      status: FuncStatus.SUCCESS,
      message: "Secrets loaded from cache.",
      data: cachedGoogleOAuthSecrets,
    };
  }

  const isEmulator = process.env.FUNCTIONS_EMULATOR === "true";

  try {
    let parsed: GoogleOAuthSecrets;
    if (isEmulator) {
      logger.log(
        "Loading secrets from local environment variables (EMULATOR Mode)"
      );
      const config = process.env.GOOGLE_OAUTH_SECRETS;
      if (!config) {
        throw new Error(`Unable to find secrets in env`);
      }

      parsed = JSON.parse(config);
    } else {
      logger.log("Loading secrets from Secret Manager without using cache");
      const secretName = "GOOGLE_OAUTH2";
      const [version] = await secretClient.accessSecretVersion({
        name: `projects/${process.env.GCLOUD_PROJECT}/secrets/${secretName}/versions/latest` /* latestじゃなくて4にしてもいいか。 */,
      });

      const data = version.payload?.data as Buffer | undefined;
      if (!data) throw new Error("Failed to load secret from Secret Manager.");

      try {
        parsed = JSON.parse(data.toString("utf8"));
      } catch {
        throw new Error("Failed to parse secret JSON.");
      }
    }

    const { clientId, clientSecret, redirectUri, encryptionKey } = parsed;
    if (!clientId || !clientSecret || !redirectUri || !encryptionKey) {
      throw new Error("Incomplete secret fields.");
    }

    cachedGoogleOAuthSecrets = parsed;
    return {
      status: FuncStatus.SUCCESS,
      message: "Google OAuth secrets loaded successfully.",
      data: parsed,
    };
  } catch (e) {
    return {
      status: FuncStatus.ERROR,
      message: e instanceof Error ? e.message : String(e),
      data: undefined,
    };
  }
};
