import axios from "axios";
import { Auth } from "firebase-admin/auth";
import { logger } from "firebase-functions";
import { MailboxExtractionService } from "../myFunc/RealtimeDbService/MailboxExtractionService";
import { Runtime } from "../shared/runtime";
import { FuncStatus } from "../type/FuncStatus";
import { MailboxGmailTokenType } from "../type/Mailbox";

export interface GmailOAuthCallbackDeps {
  auth: Pick<Auth, "verifyIdToken" | "getUser">;
  runtime: Pick<Runtime, "secrets" | "googleOAuthApi">;
  mailboxExtractionService: Pick<MailboxExtractionService, "setMailboxExtractionTokenWithEncryption">;
}

export type GmailOAuthCallbackResult =
  /** state or code is missing. */
  | "missing_parameters"
  /** The refresh token was saved. */
  | "connected"
  | "failed";

/**
 * Gmailアクセスの許可を取得したときの処理
 *
 * state is the user's Firebase ID token and code is Google's authorization code.
 * Saves the encrypted refresh token when the Gmail address is the user's sign-in address.
 */
export const handleGmailOAuthCallback = async (
  deps: GmailOAuthCallbackDeps,
  params: { state: unknown; code: unknown }
): Promise<GmailOAuthCallbackResult> => {
  logger.log("Received OAuth callback request.");
  const { state, code } = params;
  if (typeof state !== "string" || !state) {
    logger.error("State parameter is missing in the request.");
    return "missing_parameters";
  }
  if (typeof code !== "string") {
    logger.error("Code parameter is missing or invalid in the request.");
    return "missing_parameters";
  }

  try {
    /**
     * stateにFirebaseのIDトークンが入っている。
     * これをデコードして、uidを取得する。
     */
    const decodedToken = await deps.auth.verifyIdToken(state);
    if (!decodedToken || !decodedToken.uid) {
      throw new Error("Invalid state parameter: Unable to decode Firebase ID token.");
    }
    const uid = decodedToken.uid;

    const secretsResult = await deps.runtime.secrets.load();
    if (secretsResult.status !== FuncStatus.SUCCESS || !secretsResult.data) {
      throw new Error(`Failed to load Google OAuth secrets: ${secretsResult.message}`);
    }
    const secrets = secretsResult.data;

    /* アクセストークンとリフレッシュトークンを取得 */
    logger.log(`Start getting access token and refresh token...`);
    const { accessToken, refreshToken } = await deps.runtime.googleOAuthApi.exchangeCode(code, secrets);
    if (!accessToken || !refreshToken) {
      throw new Error("Unable to get access token or refresh token.");
    }

    /* Gmailアドレスを取得する */
    logger.log(`Start getting gmail....`);
    const gmailEmail = await deps.runtime.googleOAuthApi.fetchEmail(accessToken);
    if (!gmailEmail) {
      throw new Error("Unable to get Gmail email address.");
    }

    /* ここでFirebaseのGmailと一致しているかチェックし、一致していなかったら弾く */
    const userRecord = await deps.auth.getUser(uid);
    if (gmailEmail !== userRecord.email) {
      throw new Error("Permitted email and user email is different");
    }

    /* refresh_tokenを暗号化して保存する */
    const mailboxToken: MailboxGmailTokenType = {
      refreshToken,
      gmail: gmailEmail,
    };
    const saved = await deps.mailboxExtractionService.setMailboxExtractionTokenWithEncryption(
      uid,
      mailboxToken,
      secrets.encryptionKey
    );
    if (saved.status !== FuncStatus.SUCCESS) {
      throw new Error(`Failed to set mailbox extraction token for user ${uid}: ${saved.message}`);
    }
    logger.log(`Successfully set mailbox extraction token for user ${uid}.`);
    return "connected";
  } catch (err) {
    if (axios.isAxiosError(err)) {
      logger.error("Axios error:", JSON.stringify(err.response?.data ?? err.message));
    } else {
      logger.error("Unexpected error:", err);
    }
    return "failed";
  }
};
