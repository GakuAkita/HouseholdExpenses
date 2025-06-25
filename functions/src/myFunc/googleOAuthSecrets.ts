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

  const secretName = "GOOGLE_OAUTH2";
  const [version] = await secretClient.accessSecretVersion({
    name: `projects/${process.env.GCLOUD_PROJECT}/secrets/${secretName}/versions/latest`,
  });

  const data = version.payload?.data as Buffer | undefined;
  if (!data) {
    return {
      status: FuncStatus.ERROR,
      message: "Failed to load secret from Secret Manager.",
      data: undefined,
    };
  }

  let parsed: GoogleOAuthSecrets;
  try {
    parsed = JSON.parse(data.toString("utf8"));
  } catch (err) {
    return {
      status: FuncStatus.ERROR,
      message: "Failed to parse secret JSON.",
      data: undefined,
    };
  }

  const { clientId, clientSecret, redirectUri, encryptionKey } = parsed;
  if (!clientId || !clientSecret || !redirectUri || !encryptionKey) {
    return {
      status: FuncStatus.ERROR,
      message: "Incomplete secret fields.",
      data: undefined,
    };
  }

  cachedGoogleOAuthSecrets = parsed;
  return {
    status: FuncStatus.SUCCESS,
    message: "Google OAuth secrets loaded successfully.",
    data: parsed,
  };
};
