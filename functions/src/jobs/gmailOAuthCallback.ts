import axios from "axios";
import { Auth } from "firebase-admin/auth";
import { logger } from "firebase-functions";
import { MailboxExtractionService } from "../infra/rtdb/MailboxExtractionService";
import { Runtime } from "../shared/runtime";
import { MailboxGmailTokenType } from "../type/Mailbox";

/** How long the app's OAuth request stays usable after it was written. */
export const GMAIL_OAUTH_REQUEST_TTL_MS = 10 * 60 * 1000;

export interface GmailOAuthCallbackDeps {
  auth: Pick<Auth, "getUser">;
  runtime: Pick<Runtime, "secrets" | "googleOAuthApi" | "clock">;
  mailboxExtractionService: Pick<MailboxExtractionService, "saveGmailToken" | "takeGmailOAuthRequest">;
}

export type GmailOAuthCallbackResult =
  /** state or code is missing. */
  | "missing_parameters"
  /** The refresh token was saved. */
  | "connected"
  | "failed";

/**
 * Splits the OAuth state "<uid>.<nonce>". The nonce is base64url, so it has no ".".
 * Returns null when the state isn't in that form.
 */
export const parseGmailOAuthState = (state: string): { uid: string; nonce: string } | null => {
  const dot = state.lastIndexOf(".");
  if (dot <= 0 || dot === state.length - 1) {
    return null;
  }
  return { uid: state.slice(0, dot), nonce: state.slice(dot + 1) };
};

/**
 * Gmailアクセスの許可を取得したときの処理
 *
 * Before opening Google's consent page, the app writes a random nonce and a PKCE code verifier
 * to users/{uid}/mailbox_extraction/gmail_oauth_request, and sends "<uid>.<nonce>" as state.
 * Only the user can write there (database.rules.json), so a matching nonce shows that this
 * callback finishes a request the user's own app started. The request is used once.
 *
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
    const parsed = parseGmailOAuthState(state);
    if (!parsed) {
      throw new Error("Invalid state parameter.");
    }
    const { uid, nonce } = parsed;

    /* The request is removed here, so the same state can't be used again. */
    const request = await deps.mailboxExtractionService.takeGmailOAuthRequest(uid, nonce);
    if (!request) {
      throw new Error(`No OAuth request matches the state for user ${uid}.`);
    }
    const age = deps.runtime.clock.now().getTime() - request.createdAt;
    if (!(age >= 0 && age <= GMAIL_OAUTH_REQUEST_TTL_MS)) {
      throw new Error(`The OAuth request for user ${uid} has expired.`);
    }

    const secrets = await deps.runtime.secrets.load();

    /* アクセストークンとリフレッシュトークンを取得 */
    logger.log(`Start getting access token and refresh token...`);
    const { accessToken, refreshToken } = await deps.runtime.googleOAuthApi.exchangeCode(
      code,
      request.codeVerifier,
      secrets
    );
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
    await deps.mailboxExtractionService.saveGmailToken(uid, mailboxToken, secrets.encryptionKey);
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
