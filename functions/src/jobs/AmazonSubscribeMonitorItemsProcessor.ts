import { logger } from "firebase-functions";
import { findAmazonSubscribeItemId } from "../domain/amazonSubscribe";
import { createUserGmailClient } from "../infra/gmail/createUserGmailClient";
import { extractHtmlBody, extractTextBody, getSubjectFromMessage } from "../infra/gmail/extractHtmlBody";
import { filterMessages } from "../infra/gmail/filterMessages";
import { sortGmailMessagesByDate } from "../infra/gmail/getInternalDate";
import { getMessageDetailsSortedList } from "../infra/gmail/getMessageDetailsMap";
import { getAmazonSubscribeNextShipNotifyAndCancelMailIds } from "../infra/gmail/mailQueries";
import { MailboxExtractionService } from "../infra/rtdb/MailboxExtractionService";
import { AmazonSubscribeCancelParser } from "../mail/parsers/AmazonSubscribeCancelParser";
import { AmazonSubscribeNextShipmentMailParser } from "../mail/parsers/AmazonSubscribeNextShipmentMailParser";
import { messageOf } from "../shared/errors";
import { Runtime } from "../shared/runtime";
import { convertUnixMillisecToSec } from "../shared/unixTime";
import { AmazonMailSubjects } from "../type/AmazonMailSubjects";
import { AmazonSubscribeItem, createAmazonSubscribeSettingInstance } from "../type/Mailbox";

type SubscribeItems = Record<string, AmazonSubscribeItem>;

/** How far back the first run searches. */
const FIRST_RUN_LOOKBACK_MILLIS = 5 * 60 * 1000;

/**
 * Keeps one user's Amazon subscribe list up to date from Amazon's mails:
 * next-shipment and price-change mails add or update items, cancel mails remove them.
 */
export class AmazonSubscribeMonitorItemsProcessor {
  constructor(
    private userId: string,
    private mailboxExtractionService: MailboxExtractionService,
    private runtime: Runtime
  ) {}

  async handleAmazonSubscribeItems(): Promise<void> {
    const setting = await this.mailboxExtractionService.getMailTypeSetting(
      this.userId,
      createAmazonSubscribeSettingInstance()
    );
    if (!setting) {
      logger.info(`${this.userId} has never activated amazon monitor`);
      return;
    }

    const lastExec = await this.mailboxExtractionService.getAmazonSubscribeMonitorLastExec(this.userId);
    const endTime = this.runtime.clock.now().getTime();
    const startTime = lastExec?.timestamp || endTime - FIRST_RUN_LOOKBACK_MILLIS;

    const gmailClient = await createUserGmailClient(this.userId, this.mailboxExtractionService, this.runtime);
    if (!gmailClient) {
      logger.info(`Gmail Token is not set by the user`);
      return;
    }

    /* キャンセルも次回の配達通知も両方一気に取得する */
    const queryAfter = this.runtime.config.isEmulator ? 1 : convertUnixMillisecToSec(startTime);
    const msgIds = await getAmazonSubscribeNextShipNotifyAndCancelMailIds(
      gmailClient,
      queryAfter,
      convertUnixMillisecToSec(endTime),
      10
    );

    if (msgIds.length === 0) {
      /* クエリがヒットしなかった。メールが来ていない */
      logger.info(`handleAmazonSubscribeItems: Nothing was found After query.`);
      await this.mailboxExtractionService.setAmazonSubscribeMonitorLastExec(this.userId, {
        ...lastExec,
        timestamp: endTime /* UNIXミリ秒で保存 */,
      });
      return;
    }

    logger.info(`Found ${msgIds.length} msg ids`);
    const sortedList = await getMessageDetailsSortedList(gmailClient, msgIds);
    const { filteredMessages, mostRecentMsgId } = filterMessages(sortedList, lastExec?.lastMsgId);
    if (Object.keys(filteredMessages).length === 0) {
      /* Every mail found was already processed. last_exec is kept as it is. */
      logger.warn(`handleAmazonSubscribeItems: No new messages found. Probably this is not error.`);
      return;
    }

    let subscribeItems = await this.mailboxExtractionService.getAmazonSubscribeItems(this.userId);

    /* 古い順に処理していけば、リストは常に最新になる */
    for (const [id, gmail] of sortGmailMessagesByDate(filteredMessages, "asc")) {
      try {
        subscribeItems = await this.applyMail(gmail, subscribeItems);
      } catch (error) {
        logger.error(`Failed to handle the mail ${id}: ${messageOf(error)}`);
      }
    }

    await this.mailboxExtractionService.setAmazonSubscribeMonitorLastExec(this.userId, {
      timestamp: endTime,
      lastMsgId: mostRecentMsgId,
    });
  }

  /** Applies one mail to the list and returns the updated list. */
  private async applyMail(
    gmail: Parameters<typeof getSubjectFromMessage>[0],
    subscribeItems: SubscribeItems
  ): Promise<SubscribeItems> {
    const subject = getSubjectFromMessage(gmail);

    if (subject == AmazonMailSubjects.NEXT_SHIPMENT || subject == AmazonMailSubjects.PRICE_CHANGED) {
      /**
       * 普通にplain textで取得すると、価格の情報が抜けてしまうので
       * HTMLを削除してテキストを取得してから解析する
       * (価格が変わった場合のメールも同じ正規表現で行ける)
       */
      const htmlStrippedText = extractHtmlBody(gmail.payload, true);
      if (!htmlStrippedText) {
        throw new Error("Unable to extract HTML stripped Text from the mail");
      }
      const items = new AmazonSubscribeNextShipmentMailParser(htmlStrippedText).toSubscribeItem();
      for (const item of items) {
        logger.log(`Extracted Item: productName=${item.productName} price=${item.price} quantity=${item.quantity}`);
        try {
          subscribeItems = await this.addOrUpdateItem(item, subscribeItems);
        } catch (error) {
          logger.error(`Failed to update ${item.productName}: ${messageOf(error)}`);
        }
      }
      return subscribeItems;
    }

    if (subject == AmazonMailSubjects.ITEM_RUNOUT) {
      logger.log(`Item runout mail. I might handle this type of email in the future, but I ignore this so far.`);
      return subscribeItems;
    }

    if (subject?.includes(AmazonMailSubjects.CANCELED_SUBSCRIPTION)) {
      const rawText = extractTextBody(gmail.payload);
      if (!rawText) {
        throw new Error("Unable to extract Text from the mail");
      }
      const productName = new AmazonSubscribeCancelParser(rawText).extractProductName();
      if (!productName) {
        throw new Error(`Unable to parser from Cancel Subscription Mail!!`);
      }
      return this.removeItem({ productName }, subscribeItems);
    }

    logger.log(`This is unknown subject:${subject}`);
    return subscribeItems;
  }

  /** Adds the item, or updates the registered one when its price or quantity changed or it was disabled. */
  async addOrUpdateItem(item: AmazonSubscribeItem, itemMap: SubscribeItems): Promise<SubscribeItems> {
    const id = findAmazonSubscribeItemId(item.productName, itemMap);
    if (id === null) {
      logger.log(`New item: ${item.productName} should be added.`);
      const added = await this.mailboxExtractionService.addAmazonSubscribeItem(this.userId, item);
      return { ...itemMap, [added.id!]: added };
    }

    const existingItem = itemMap[id];
    /* enabled=falseの場合はtrueに戻す */
    const needsEnableUpdate = existingItem.enabled === false;
    if (existingItem.price === item.price && existingItem.quantity === item.quantity && !needsEnableUpdate) {
      logger.log(`No need to update AmazonSubscribeItem!!`);
      return itemMap;
    }

    logger.log(`This item:${existingItem.productName} must be updated.`);
    const updatedItem: AmazonSubscribeItem = {
      ...existingItem,
      price: item.price,
      quantity: item.quantity,
      enabled: needsEnableUpdate ? true : existingItem.enabled,
    };
    await this.mailboxExtractionService.updateAmazonSubscribeItem(this.userId, updatedItem);
    return { ...itemMap, [id]: updatedItem };
  }

  /** Removes the registered item for the product. */
  async removeItem(item: AmazonSubscribeItem, itemMap: SubscribeItems): Promise<SubscribeItems> {
    const id = findAmazonSubscribeItemId(item.productName, itemMap);
    if (id === null) {
      /**
       * 名前が若干変わっている可能性がある。その場合、リストから消せないので手動で消すしかない。
       * 端末に通知を行いたい。
       */
      logger.warn(`Attempted to remove from Subscribe items, but not exist. ${item.productName}`);
      return itemMap;
    }
    await this.mailboxExtractionService.removeAmazonSubscribeItem(this.userId, itemMap[id]);
    logger.log(`Removed ${itemMap[id].productName} from Subscribe items.`);
    const { [id]: _removed, ...rest } = itemMap;
    return rest;
  }
}
