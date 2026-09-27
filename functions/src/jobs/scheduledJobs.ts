import { logger } from "firebase-functions";
import { AmazonSubscribeMonitorItemsProcessor } from "./AmazonSubscribeMonitorItemsProcessor";
import { MailboxExtractionProcessor } from "./MailboxExtractionProcessor";
import { Services } from "../app/services";
import { FuncStatus } from "../type/FuncStatus";
import { AllMailType } from "../type/Mailbox";
import { forEachUser } from "./forEachUser";

/** Monthly: adds this month's expenses from every user's RepeatAdds. */
export const runRepeatAddJob = async (
  services: Pick<Services, "userService" | "repeatAddProcessor">
): Promise<void> => {
  await forEachUser(services.userService, async (userId) => {
    const addResult = await services.repeatAddProcessor.addExpensesFromAllRepeatAdd(userId);
    if (addResult.status !== FuncStatus.SUCCESS) {
      logger.error(
        `Failed to add expenses from repeat adds for user ${userId}: ${addResult.message}`
      );
    } else {
      logger.log(`Successfully added expenses from repeat adds for user ${userId}.`);
    }
  });
};

/** Reads each user's mails of the given types and saves the expenses in them. */
export const runMailboxExtractionJob = async (
  services: Pick<
    Services,
    | "userService"
    | "mailboxExtractionService"
    | "expenseService"
    | "categoryService"
    | "categoryAssignmentService"
    | "runtime"
  >,
  mailTypes: AllMailType[]
): Promise<void> => {
  await forEachUser(services.userService, async (userId) => {
    /* ユーザーごとにインスタンスを生成 */
    const processor = new MailboxExtractionProcessor(
      userId,
      services.mailboxExtractionService,
      services.expenseService,
      services.categoryService,
      services.categoryAssignmentService,
      services.runtime
    );
    await processor.processAllMailTypeList(mailTypes);
  });
};

/** 定期便リストの更新: reads each user's Amazon subscription mails and updates their list. */
export const runAmazonSubscribeMonitorJob = async (
  services: Pick<Services, "userService" | "mailboxExtractionService" | "runtime">
): Promise<void> => {
  await forEachUser(services.userService, async (userId) => {
    const processor = new AmazonSubscribeMonitorItemsProcessor(
      userId,
      services.mailboxExtractionService,
      services.runtime
    );
    const result = await processor.handleAmazonSubscribeItems();
    if (result.status !== FuncStatus.SUCCESS) {
      logger.error(`Failed to handle Amazon Subscribe items: ${result.message ?? "No message"}`);
    }
  });
};
