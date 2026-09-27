import { logger } from "firebase-functions";
import { AmazonItemDispatchedMailParser } from "../parsers/AmazonItemDispatchedMailParser";
import { getAmazonDispatchedMailIds } from "../../infra/gmail/mailQueries";
import { isAmazonSubscribeProductExist } from "../../domain/isAmazonSubscribeProductExist";
import { Expense } from "../../type/Expense";
import { FuncStatus } from "../../type/FuncStatus";
import { AmazonSubscribeSetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { assignByProductName, extracted, failed, missingInternalDate, noDataAttached } from "./common";

/**
 * Amazon定期便: reads "発送済み" mails and keeps only the products in the user's subscribe list.
 * Other dispatched products are normal orders, which amazon_item already records.
 */
export const amazonSubscribeSource: MailSource<AmazonSubscribeSetting> = {
  nodeName: "amazon_subscribe",

  /* Dispatched mails are frequent, so up to 10 per run. */
  findMailIds: (gmail, after, before) => getAmazonDispatchedMailIds(gmail, after, before, 10),

  async toExpenses(mail, _setting, context) {
    if (!mail.internalDate) return missingInternalDate("AmazonSubscribe");
    const parsed = new AmazonItemDispatchedMailParser(mail.rawText, mail.internalDate).toExpenses();
    if (parsed.status !== FuncStatus.SUCCESS) return failed(parsed);
    if (!parsed.data) return noDataAttached("AmazonSubscribe");

    const itemsResult = await context.loadEnabledAmazonSubscribeItems();
    if (itemsResult.status === FuncStatus.EMPTY) {
      logger.warn(`Amazon Subscirbe items are not registered, yet.`);
      return extracted([]);
    }
    if (itemsResult.status !== FuncStatus.SUCCESS) {
      return failed({
        status: FuncStatus.ERROR,
        message: `Failed to load Amazon Subscribe items: ${itemsResult.message}`,
      });
    }
    const subscribeItems = itemsResult.data ?? {};

    const expenses: Expense[] = [];
    for (const expense of parsed.data) {
      if (!expense.itemName) {
        logger.warn(`Expense has no itemName, skipping: ${JSON.stringify(expense)}`);
        continue;
      }
      const exists = isAmazonSubscribeProductExist({ productName: expense.itemName }, subscribeItems);
      if (exists.status !== FuncStatus.SUCCESS) {
        logger.debug(`Item "${expense.itemName}" is not in Amazon Subscribe list, skipping`);
        continue;
      }
      expenses.push(assignByProductName(expense, context));
    }
    return extracted(expenses);
  },
};
