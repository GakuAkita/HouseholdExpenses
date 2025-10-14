import { logger } from "firebase-functions";
import { AmazonMailSubjects } from "../../type/AmazonMailSubjects";
import { FuncResult, FuncStatus } from "../../type/FuncStatus";
import {
  createAmazonSubscribeSettingInstance,
  LastMailboxExtractionExec,
} from "../../type/Mailbox";
import { GmailApiClient } from "../Client/GmailApiClient";
import { AmazonSubscribeNextShipmentMailParser } from "../Parser/AmazonSubscribeParsre";
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

  async updateAmazonSubscribeItems(): Promise<FuncResult> {
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
      /**
       * GmailAPIが有効になっている限りは定期便の更新は行っておく
       */
      /* 特に問題ないので次へ */
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
      const subscribeItems = itemsRet.data;

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
          logger.debug(`${parser.extractProductName()}`);
        } else if (subject == AmazonMailSubjects.ITEM_RUNOUT) {
          logger.log(`This is item runout mail`);
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
}
