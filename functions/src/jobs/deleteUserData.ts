import { UserService } from "../infra/firestore/UserService";
import { UserRTDbService } from "../infra/rtdb/UserRTDbService";
import { GmailRevokeDeps, revokeGmailTokens } from "./gmailDisconnect";

export interface DeleteUserDataDeps extends GmailRevokeDeps {
  userService: Pick<UserService, "deleteUser">;
  userRTDbService: Pick<UserRTDbService, "deleteUser">;
}

/**
 * Removes everything stored for a deleted user: Gmail access, Firestore and Realtime Database.
 *
 * Throws on failure, so the trigger runs it again. Every step can safely run twice. The Realtime
 * Database goes last because it holds the Gmail token, which a retry still needs to revoke it.
 */
export const deleteUserData = async (deps: DeleteUserDataDeps, userId: string): Promise<void> => {
  await revokeGmailTokens(deps, userId);
  await deps.userService.deleteUser(userId);
  await deps.userRTDbService.deleteUser(userId);
};
