import { logger } from "firebase-functions";
import { findAmazonSubscribeItemId } from "../../domain/amazonSubscribe";
import { getAmazonDispatchedMailIds } from "../../infra/gmail/mailQueries";
import { Expense } from "../../type/Expense";
import { AmazonSubscribeItem, AmazonSubscribeSetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { AmazonItemDispatchedMailParser } from "../parsers/AmazonItemDispatchedMailParser";
import { assignByProductName, requireInternalDate } from "./common";

/** Whether the product is in the subscribe list. An invalid list entry is logged and treated as no match. */
const isSubscribed = (productName: string, items: Record<string, AmazonSubscribeItem>): boolean => {
  try {
    return findAmazonSubscribeItemId(productName, items) !== null;
  } catch (error) {
    logger.error(`Failed to check the Amazon Subscribe list: ${error}`);
    return false;
  }
};

/**
 * Amazon定期便: reads "発送済み" mails and keeps only the products in the user's subscribe list.
 * Other dispatched products are normal orders, which amazon_item already records.
 */
export const amazonSubscribeSource: MailSource<AmazonSubscribeSetting> = {
  nodeName: "amazon_subscribe",

  /* Dispatched mails are frequent, so up to 10 per run. */
  findMailIds: (gmail, after, before) => getAmazonDispatchedMailIds(gmail, after, before, 10),

  async toExpenses(mail, _setting, context) {
    const internalDate = requireInternalDate(mail.internalDate, "AmazonSubscribe");
    const dispatched = new AmazonItemDispatchedMailParser(mail.rawText, internalDate).toExpenses();

    const subscribeItems = await context.loadEnabledAmazonSubscribeItems();
    if (Object.keys(subscribeItems).length === 0) {
      logger.warn(`Amazon Subscirbe items are not registered, yet.`);
      return [];
    }

    const expenses: Expense[] = [];
    for (const expense of dispatched) {
      if (!expense.itemName) {
        logger.warn(`Expense has no itemName, skipping: ${JSON.stringify(expense)}`);
        continue;
      }
      if (!isSubscribed(expense.itemName, subscribeItems)) {
        logger.debug(`Item "${expense.itemName}" is not in Amazon Subscribe list, skipping`);
        continue;
      }
      expenses.push(assignByProductName(expense, context));
    }
    return expenses;
  },
};
