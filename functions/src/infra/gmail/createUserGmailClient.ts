import { Runtime } from "../../shared/runtime";
import { MailboxExtractionService } from "../rtdb/MailboxExtractionService";
import { GmailClient } from "./GmailApiClient";

/**
 * A Gmail client for the user's connected Gmail, or null when the user hasn't connected Gmail.
 *
 * Throws when the secrets can't be loaded. If Secret Manager says
 * "7 PERMISSION_DENIED: Permission 'secretmanager.versions.access' denied",
 * the compute service account needs the Secret Manager Secret Accessor role.
 */
export async function createUserGmailClient(
  userId: string,
  mailboxExtractionService: Pick<MailboxExtractionService, "getGmailToken">,
  runtime: Pick<Runtime, "secrets" | "createGmailClient">
): Promise<GmailClient | null> {
  /* Google認証に必要な情報+暗号化キーをロードする */
  const secrets = await runtime.secrets.load();

  /* RealtimeDBにrefreshTokenがあるかチェックする。なければ、そこで終了 */
  const token = await mailboxExtractionService.getGmailToken(userId, secrets.encryptionKey);
  if (!token) {
    return null;
  }

  return runtime.createGmailClient({
    clientId: secrets.clientId,
    clientSecret: secrets.clientSecret,
    refreshToken: token.refreshToken,
  });
}
