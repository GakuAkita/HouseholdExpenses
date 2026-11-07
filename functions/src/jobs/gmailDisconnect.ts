import { logger } from "firebase-functions";
import { https } from "firebase-functions/v1";
import { MailboxExtractionService } from "../infra/rtdb/MailboxExtractionService";
import { decryptWithKey } from "../shared/encryption";
import { messageOf } from "../shared/errors";
import { Runtime } from "../shared/runtime";

export interface GmailRevokeDeps {
  runtime: Pick<Runtime, "secrets" | "googleOAuthApi">;
  mailboxExtractionService: Pick<MailboxExtractionService, "getEncryptedGmailRefreshTokens">;
}

export interface GmailDisconnectDeps extends GmailRevokeDeps {
  mailboxExtractionService: Pick<
    MailboxExtractionService,
    "getEncryptedGmailRefreshTokens" | "removeGmailTokens"
  >;
}

/**
 * Invalidates the user's stored refresh tokens at Google. The tokens stay in the database.
 *
 * Throws when Google can't be reached, so the caller keeps the token and can try again: once
 * the stored copy is gone, the token can't be revoked anymore. A token that can't be decrypted
 * can't be revoked now or later, so it is logged and skipped.
 */
export const revokeGmailTokens = async (deps: GmailRevokeDeps, userId: string): Promise<void> => {
  const encryptedTokens = await deps.mailboxExtractionService.getEncryptedGmailRefreshTokens(userId);
  if (encryptedTokens.length === 0) {
    return;
  }

  const secrets = await deps.runtime.secrets.load();
  for (const encryptedToken of encryptedTokens) {
    let refreshToken: string;
    try {
      refreshToken = decryptWithKey(encryptedToken, secrets.encryptionKey);
    } catch (error) {
      logger.error(`Can't revoke a Gmail token of user ${userId}, it can't be decrypted: ${messageOf(error)}`);
      continue;
    }
    const result = await deps.runtime.googleOAuthApi.revokeToken(refreshToken);
    logger.log(`Gmail token of user ${userId}: ${result}`);
  }
};

/** Revokes the user's Gmail access at Google, then deletes the stored tokens. */
export const disconnectGmail = async (deps: GmailDisconnectDeps, userId: string): Promise<void> => {
  await revokeGmailTokens(deps, userId);
  await deps.mailboxExtractionService.removeGmailTokens(userId);
};

/** The app's call to disconnect Gmail. userId comes from the caller's sign-in, not from the request. */
export const handleDisconnectGmailCall = async (
  deps: GmailDisconnectDeps,
  userId: string | undefined
): Promise<void> => {
  if (!userId) {
    throw new https.HttpsError("unauthenticated", "Please sign in to disconnect Gmail.");
  }
  try {
    await disconnectGmail(deps, userId);
  } catch (error) {
    logger.error(`Failed to disconnect Gmail for user ${userId}: ${messageOf(error)}`);
    throw new https.HttpsError("unavailable", "Couldn't disconnect Gmail. Please try again.");
  }
};
