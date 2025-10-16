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
import { AmazonSubscribeNextShipmentMailParser } from "../Parser/AmazonSubscribeNextShipmentMailParser";
import { MailboxExtractionService } from "../RealtimeDbService/MailboxExtractionService";
import {
  convertUnixMillisecToSec,
  getCurrentUnixMillisec,
} from "../utility/getCurrentUnixSec";
import {
  extractTextBody,
  getSubjectFromMessage,
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
    const funcName = "updateAmazonSubscribeList";

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
      5
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
        logger.error(`${funcName}:${filterRet.message}`);
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
      const subscribeItems =
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
        // logger.log(`${extractTextBody(gmail.payload)}`);

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
          const parser = new AmazonSubscribeNextShipmentMailParser(rawText);
          const ret = parser.toSubscribeItem();
          if (ret.status != FuncStatus.SUCCESS) {
            logger.error(`${ret.message}`);
            continue;
          }

          const item: AmazonSubscribeItem = ret.data!;
          /**
           * アイテムの中に製品名があるかチェックする
           * 価格、個数
           */
          const updateRet = await this.updateAmazonSubscribeItems(
            item,
            subscribeItems
          );
        } else if (subject == AmazonMailSubjects.ITEM_RUNOUT) {
          logger.log(`-------This is item runout mail--------`);
          logger.log(
            `I might handle this type of email in the future, but I ignore this so far.`
          );
        } else if (
          subject?.includes(AmazonMailSubjects.CANCELED_SUBSCRIPTION)
        ) {
          logger.log(`This is cancel mail`);
        } else {
          logger.log(`This is unknown subject:${subject}`);
          continue;
        }

        /* Parseする */
        /**
         * if(subject=="次回、、"){
         * subscribeItemsの中に同じのがないかチェックして、なければadd、あればupdate
         * }
         * "定期購入をキャンセル"
         * subscribeItemsの中になければスルー、あればremove
         */
        /* subscribeItemsにすでに存在するかチェックする */
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
  ): Promise<FuncResult> {
    let ret: FuncResult;
    const existRet = isAmazonSubscribeProductExist(item, itemMap);
    if (existRet.status == FuncStatus.ERROR) {
      ret = existRet;
    } else if (existRet.status == FuncStatus.EMPTY) {
      ret = {
        status: FuncStatus.SUCCESS,
        message: `Attempted to remove from Subscribe items, but not exist. ${item.productName}`,
      };
    } else if (existRet.status == FuncStatus.SUCCESS) {
      const id = existRet.data!;
      const removedItem = itemMap[id];
      ret =
        await this.mailboxExtractionService.removeAmazonSubscribeMonitorItem(
          this.userId,
          removedItem
        );
    } else {
      ret = {
        status: FuncStatus.ERROR,
        message: `This is the bug. ${item.productName}`,
      };
    }

    return ret;
  }
}
