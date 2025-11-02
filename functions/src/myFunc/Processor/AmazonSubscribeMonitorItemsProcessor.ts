import { logger } from "firebase-functions";
import { AmazonMailSubjects } from "../../type/AmazonMailSubjects";
import {
  FuncResult,
  FuncResultWithData,
  FuncStatus,
  toFuncResult,
} from "../../type/FuncStatus";
import {
  AmazonSubscribeItem,
  createAmazonSubscribeSettingInstance,
  LastMailboxExtractionExec,
} from "../../type/Mailbox";
import { GmailApiClient } from "../Client/GmailApiClient";
import { AmazonSubscribeCancelParser } from "../Parser/AmazonSubscribeCancelParser";
import { AmazonSubscribeNextShipmentMailParser } from "../Parser/AmazonSubscribeNextShipmentMailParser";
import { MailboxExtractionService } from "../RealtimeDbService/MailboxExtractionService";
import {
  convertUnixMillisecToSec,
  getCurrentUnixMillisec,
} from "../utility/getCurrentUnixSec";
import {
  extractHtmlBody,
  extractTextBody,
  getSubjectFromMessage,
  stripHtmlTags,
} from "../utility/gmail/extractHtmlBody";
import { filterMessages } from "../utility/gmail/filterMessages";
import { generateGmailApiInstance } from "../utility/gmail/generateGmailApiInstance";
import { sortGmailMessagesByDate } from "../utility/gmail/getInternalDate";
import { getMessageDetailsSortedList } from "../utility/gmail/getMessageDetailsMap";
import { getAmazonSubscribeNextShipNotifyAndCancelMailIds } from "../utility/gmail/mailQueries";
import { isAmazonSubscribeProductExist } from "../utility/isAmazonSubscribeProductExist";

/**
 * Gmailをモニターし、
 * Amazon定期便のリストを更新
 */
export class AmazonSubscribeMonitorItemsProcessor {
  private userId: string;

  constructor(
    userId: string,
    private mailboxExtractionService: MailboxExtractionService
  ) {
    this.userId = userId;
  }

  async handleAmazonSubscribeItems(): Promise<FuncResult> {
    const funcName = "handleAmazonSubscribeList";

    const type = createAmazonSubscribeSettingInstance();
    let ret =
      await this.mailboxExtractionService.getMailboxExtractionMailTypeSetting(
        this.userId,
        type
      );
    if (ret.status == FuncStatus.EMPTY) {
      logger.info(`${this.userId} has never activated amazon monitor`);
      return {
        status: FuncStatus.SUCCESS,
      };
    } else if (ret.status != FuncStatus.SUCCESS || !ret.data) {
      /**
       * なにかエラーが出たようだ
       */
      logger.error(
        `${type.nodeName} went wrong when getting setting.: ${ret.message}`
      );
      return ret;
    } else {
      /* 特に問題ないので次へ */
      logger.debug(`The user has Amazon Subscribe setting`);
    }

    const lastExecRet =
      await this.mailboxExtractionService.getAmazonSubscribeMonitorLastExec(
        this.userId
      );
    if (lastExecRet.status != FuncStatus.SUCCESS) {
      logger.error(`${lastExecRet.message}`);
      return lastExecRet;
    }

    const endTime = getCurrentUnixMillisec();
    const lastMsgId = lastExecRet.data?.lastMsgId;
    let startTime: number = 0;
    if (!lastExecRet.data?.timestamp) {
      startTime = endTime - 60 * 5 * 1000;
    } else {
      startTime =
        lastExecRet.data.timestamp; /* タイムスタンプがあるならそれを使う */
    }

    const gmailClientRet = await generateGmailApiInstance(
      this.userId,
      this.mailboxExtractionService
    );
    if (gmailClientRet.status != FuncStatus.SUCCESS || !gmailClientRet.data) {
      logger.error(`${gmailClientRet.message}`);
      return gmailClientRet;
    }
    const gmailClient: GmailApiClient = gmailClientRet.data;
    /**
     * クエリをして、msgIdを取得
     */
    const isEmulator = process.env.FUNCTIONS_EMULATOR === "true";

    /**
     * こっちは次回配送の連絡
     */
    const queryAfter = isEmulator ? 1 : convertUnixMillisecToSec(startTime);
    const queryBefore = convertUnixMillisecToSec(endTime);
    /* キャンセルも次回の配達通知も両方一気に取得する */
    const queryRet = await getAmazonSubscribeNextShipNotifyAndCancelMailIds(
      gmailClient,
      queryAfter,
      queryBefore,
      10
    );

    if (!queryRet.data || queryRet.data.length === 0) {
      /* クエリがヒットしなかった。メールが来ていない */
      logger.info(`${funcName}:Nothing was found After query.`);
      const newLastExec: LastMailboxExtractionExec = {
        ...lastExecRet.data,
        timestamp: endTime /* UNIXミリ秒で保存 */,
      };
      const ret =
        await this.mailboxExtractionService.setAmazonSubscribeMonitorLastExec(
          this.userId,
          newLastExec
        );
      if (ret.status != FuncStatus.SUCCESS) {
        logger.error(`${ret.message}`);
      } else {
        logger.log(`${funcName} : Updated:${JSON.stringify(newLastExec)}`);
      }
    } else {
      /**
       * メールがあったので処理をする
       * */
      const hitMsgIds = queryRet.data;
      logger.info(`Found ${hitMsgIds.length} msg ids`);
      const sortRet = await getMessageDetailsSortedList(gmailClient, hitMsgIds);
      if (sortRet.status != FuncStatus.SUCCESS || !sortRet.data) {
        logger.error(`${sortRet.message}`);
        return sortRet;
      }

      const sortedList = sortRet.data;
      const filterRet = filterMessages(sortedList, lastMsgId);
      if (filterRet.status != FuncStatus.SUCCESS) {
        if (filterRet.status == FuncStatus.EMPTY) {
          /* 検索をしていくつかヒットしたけどlastMsgIdでフィルターしたときに何も残らなかった */
          /* めったに起きないはず */
          logger.warn(`${funcName}: Probably this is not error.`);
        }
        logger.error(
          `${funcName}:${filterRet.message ? filterRet.message : "No message"}`
        );
        return filterRet;
      }

      const filteredMessages = filterRet.data?.filteredMessages;
      const mostRecentMsgId = filterRet.data?.mostRecentMsgId;
      if (!filteredMessages) {
        logger.warn(`${funcName}:filteredMessages is null...`);
        return {
          status: FuncStatus.ERROR,
          message: `${funcName}:filteredMessages is null...`,
        };
      }

      /**
       * 定期便リストを取得
       */
      const itemsRet =
        await this.mailboxExtractionService.getAmazonSubscribeMonitorItems(
          this.userId
        );
      if (itemsRet.status == FuncStatus.ERROR) {
        return itemsRet;
      }

      /* EMPTYの場合は{}が返って来る */
      let subscribeItems =
        itemsRet.status == FuncStatus.EMPTY ? {} : itemsRet.data!;

      /**
       * filteredMessagesには新しい次回の配送についてと定期便キャンセルの両方が
       * 含まれている
       * ここで一旦さらにsortしておく。一度mapにしてしまったので。
       * 古い順に並べてあるので順番にキャンセルなり保存を繰り返していけば常に最新になる
       */
      const filteredList = sortGmailMessagesByDate(filteredMessages, "asc");

      for (const [_, gmail] of filteredList) {
        // logger.log("-------------------");
        // logger.log(`${getSubjectFromMessage(gmail)}`);
        // logger.log("***************************");
        //console.log(`${extractTextBody(gmail.payload)}`);

        const rawText = extractTextBody(gmail.payload);
        if (!rawText) {
          logger.error("Unable to extract Text from the mail");
          continue;
        }

        const subject = getSubjectFromMessage(gmail);
        if (
          subject == AmazonMailSubjects.NEXT_SHIPMENT ||
          subject ==
          AmazonMailSubjects.PRICE_CHANGED /* 価格が変わった場合のメールも正規表現は同じで行ける */
        ) {
          /**
           *  普通にplain textで取得すると、価格の情報が抜けてしまうので
           *  HTMLを削除してテキストを取得する
           *  その後、AmazonSubscribeNextShipmentMailParserで解析する
           */
          let htmlStrippedText = extractHtmlBody(gmail.payload, true);
          if (!htmlStrippedText) {
            logger.error("Unable to extract HTML stripped Text from the mail");
            continue;
          }

          const parser = new AmazonSubscribeNextShipmentMailParser(htmlStrippedText);
          const ret = parser.toSubscribeItem();
          if (ret.status != FuncStatus.SUCCESS) {
            logger.error(`${ret.message}`);
            continue;
          }

          const items: AmazonSubscribeItem[] = ret.data!;
          for (const item of items) {
            logger.log(
              `Extracted Item: productName=${item.productName} price=${item.price} quantity=${item.quantity}`
            );

            /**
             * アイテムの中に製品名があるかチェックする
             * 価格、個数
             */
            const updateRet = await this.updateAmazonSubscribeItems(
              item,
              subscribeItems
            );

            if (updateRet.status == FuncStatus.SUCCESS) {
              /* 成功の場合のみここでmapを更新 */
              subscribeItems = updateRet.data!;
            }
          }
        } else if (subject == AmazonMailSubjects.ITEM_RUNOUT) {
          logger.log(`-------This is item runout mail--------`);
          logger.log(
            `I might handle this type of email in the future, but I ignore this so far.`
          );
        } else if (
          subject?.includes(AmazonMailSubjects.CANCELED_SUBSCRIPTION)
        ) {
          //logger.log(`${extractTextBody(gmail.payload)}`);
          const rawText = extractTextBody(gmail.payload);
          if (!rawText) {
            logger.error("Unable to extract Text from the mail");
            continue;
          }
          logger.log(`!!!!!!!!!!!!This is cancel mail!!!!!!!!!!!!!!!`);
          const parser = new AmazonSubscribeCancelParser(rawText);
          const productName = parser.extractProductName();
          if (!productName) {
            logger.error(`Unable to parser from Cancel Subscription Mail!!`);
            continue;
          }
          const item: AmazonSubscribeItem = {
            productName: productName,
          };
          const removeRet = await this.removeFromAmazonSubscribeItems(
            item,
            subscribeItems
          );
          if (removeRet.status == FuncStatus.SUCCESS) {
            logger.debug(
              `Removed ${item.productName} ${removeRet?.message ? removeRet.message : "No message"
              }`
            );
            subscribeItems = removeRet.data!;
          } else if (removeRet.status == FuncStatus.EMPTY) {
            logger.warn(
              `${removeRet.message ? removeRet.message : "No message"}`
            );
            /**
             * 名前が若干変わっている可能性がある。
             * その場合、リストから消せないので手動で消すしかない。
             * 端末に通知を行いたい、
             */
          } else {
            logger.log(
              `Something went wrong: ${removeRet.message ? removeRet.message : "No message"
              }`
            );
          }
        } else {
          logger.log(`This is unknown subject:${subject}`);
          continue;
        }
      }

      /* 最後にlastIdを保存する */
      /* 最後にlastExecを更新する */
      const lastExec: LastMailboxExtractionExec = {
        timestamp: endTime,
        lastMsgId: mostRecentMsgId,
      };
      const ret =
        await this.mailboxExtractionService.setAmazonSubscribeMonitorLastExec(
          this.userId,
          lastExec
        );
      if (ret.status != FuncStatus.SUCCESS) {
        logger.error(`${ret.message}`);
      }
    }

    return {
      status: FuncStatus.SUCCESS,
    };
  }

  /**
   * 既存のitemMapで被っているのがないか確認し、
   * 場合によっては追加する
   */
  async updateAmazonSubscribeItems(
    item: AmazonSubscribeItem,
    itemMap: Record<string, AmazonSubscribeItem>
  ): Promise<FuncResultWithData<Record<string, AmazonSubscribeItem>>> {
    /* 追加/updateしたときに新しいMapを返す */
    let ret: FuncResultWithData<Record<string, AmazonSubscribeItem>>;
    const existRet = isAmazonSubscribeProductExist(item, itemMap);
    if (existRet.status == FuncStatus.ERROR) {
      /* 何かしらのエラー */
      ret = toFuncResult(existRet);
    } else if (existRet.status == FuncStatus.EMPTY) {
      /* 存在しないので新規追加 */
      const addRet =
        await this.mailboxExtractionService.addAmazonSubscribeMonitorItem(
          this.userId,
          item
        );
      if (addRet.status == FuncStatus.SUCCESS) {
        /* 成功したのでMapに追加 */
        const newItem = addRet.data!;
        const newItemMap = {
          ...itemMap,
          [newItem.id!]: newItem,
        };

        ret = {
          status: FuncStatus.SUCCESS,
          data: newItemMap,
        };
      } else {
        ret = toFuncResult(addRet);
      }
    } else if (existRet.status == FuncStatus.SUCCESS) {
      const id = existRet.data!;
      const existingItem = itemMap[id];
      if (
        existingItem.price !== item.price ||
        existingItem.quantity !== item.quantity
      ) {
        /* 価格と個数が違えばupdate */
        const updatedItem: AmazonSubscribeItem = {
          ...existingItem,
          price: item.price,
          quantity: item.quantity,
        };

        const updateRet =
          await this.mailboxExtractionService.updateAmazonSubscribeMonitorItem(
            this.userId,
            updatedItem
          );
        if (updateRet.status == FuncStatus.SUCCESS) {
          const newItemMap = {
            ...itemMap,
            [updatedItem.id!]: updatedItem,
          };
          ret = {
            status: FuncStatus.SUCCESS,
            data: newItemMap,
          };
        } else {
          ret = toFuncResult(updateRet);
        }
      } else {
        logger.log(`No need to update AmazonSubscribeItem!!`);
        ret = {
          status: FuncStatus.SUCCESS,
          message: "No need to update AmazonSubscribeItem",
          data: itemMap,
        };
      }
    } else {
      ret = {
        status: FuncStatus.ERROR,
        message: `This is the bug in editAmazonSubscribeItemData`,
      };
    }
    return ret;
  }

  async removeFromAmazonSubscribeItems(
    item: AmazonSubscribeItem,
    itemMap: Record<string, AmazonSubscribeItem>
  ): Promise<FuncResultWithData<Record<string, AmazonSubscribeItem>>> {
    let ret: FuncResultWithData<Record<string, AmazonSubscribeItem>>;
    const existRet = isAmazonSubscribeProductExist(item, itemMap);
    if (existRet.status == FuncStatus.ERROR) {
      ret = toFuncResult(existRet);
    } else if (existRet.status == FuncStatus.EMPTY) {
      ret = {
        status: FuncStatus.EMPTY,
        message: `Attempted to remove from Subscribe items, but not exist. ${item.productName}`,
        data: itemMap,
      };
    } else if (existRet.status == FuncStatus.SUCCESS) {
      const id = existRet.data!;
      const removedItem = itemMap[id];
      const removeRet =
        await this.mailboxExtractionService.removeAmazonSubscribeMonitorItem(
          this.userId,
          removedItem
        );
      if (removeRet.status == FuncStatus.SUCCESS) {
        logger.log(`Successfully Removed from Subscribe items.`);

        // itemMap から削除済みアイテムを反映した新しい Map を作る
        const newItemMap = { ...itemMap };
        delete newItemMap[id];
        return {
          status: FuncStatus.SUCCESS,
          data: newItemMap,
          message: `Removed ${removedItem.productName} successfully`,
        };
      } else {
        /* 削除に失敗したらそのまま */
        ret = toFuncResult(removeRet);
      }
    } else {
      ret = {
        status: FuncStatus.ERROR,
        message: `This is the bug. ${item.productName}`,
      };
    }

    return ret;
  }
}
