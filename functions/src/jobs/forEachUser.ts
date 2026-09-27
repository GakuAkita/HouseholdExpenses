import { logger } from "firebase-functions";
import { UserService } from "../infra/firestore/UserService";
import { messageOf } from "../shared/errors";

/**
 * Runs the task for every user, one after another.
 * An exception for one user is logged and doesn't stop the others.
 */
export const forEachUser = async (
  userService: Pick<UserService, "getAllUserIds">,
  task: (userId: string) => Promise<void>
): Promise<void> => {
  let userIds: string[];
  try {
    /* ユーザーIDをすべて取得してくる */
    userIds = await userService.getAllUserIds();
  } catch (error) {
    logger.error("Failed to retrieve user IDs:", messageOf(error));
    return;
  }
  logger.log(`Found ${userIds.length} users.`);

  for (const userId of userIds) {
    try {
      await task(userId);
    } catch (error) {
      logger.error(`Failed for user ${userId}: ${messageOf(error)}`);
    }
  }
};
