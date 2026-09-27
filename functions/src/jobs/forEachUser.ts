import { logger } from "firebase-functions";
import { UserService } from "../myFunc/FirestoreService/UserService";
import { FuncStatus } from "../type/FuncStatus";

/**
 * Runs the task for every user, one after another.
 * An exception for one user is logged and doesn't stop the others.
 */
export const forEachUser = async (
  userService: Pick<UserService, "getAllUserIds">,
  task: (userId: string) => Promise<void>
): Promise<void> => {
  /* ユーザーIDをすべて取得してくる */
  const funcResult = await userService.getAllUserIds();
  if (funcResult.status !== FuncStatus.SUCCESS) {
    logger.error("Failed to retrieve user IDs:", funcResult.message);
    return;
  }

  const userIds = funcResult.data;
  if (userIds == null) {
    logger.error("No user IDs found.");
    return;
  }
  logger.log(`Found ${userIds.length} users.`);

  for (const userId of userIds) {
    try {
      await task(userId);
    } catch (error) {
      logger.error(`Unexpected error for user ${userId}:`, error);
    }
  }
};
