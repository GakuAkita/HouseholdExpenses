import { logger } from "firebase-functions";
import { FuncResult, FuncStatus } from "../../type/FuncStatus";
import {
  AmazonSubscribeSetting,
  createAmazonSubscribeSettingInstance,
} from "../../type/Mailbox";
import { GmailApiClient } from "../Client/GmailApiClient";
import { MailboxExtractionService } from "../RealtimeDbService/MailboxExtractionService";
import {
  convertUnixMillisecToSec,
  getCurrentUnixMillisec,
} from "../utility/getCurrentUnixSec";
import { generateGmailApiInstance } from "../utility/gmail/generateGmailApiInstance";
import { getAmazonNextShipNotifyMailIds } from "../utility/gmail/mailQueries";

/**
 * Gmailをモニターし、
 * Amazon定期便のリストを更新
 */
class AmazonSubscribeListProcessor {
  private userId: string;

  constructor(
    userId: string,
    private mailboxExtractionService: MailboxExtractionService
  ) {
    this.userId = userId;
  }

  async handleAmazonSubscribeList(): Promise<FuncResult> {
    const type = createAmazonSubscribeSettingInstance();
    let ret =
      await this.mailboxExtractionService.getMailboxExtractionMailTypeSetting(
        this.userId,
        type
      );
    if (ret.status == FuncStatus.EMPTY) {
      logger.info(`${this.userId} doesn't allow Gmail Extraction`);
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
    } else if (ret.data?.enabled == false) {
      /**
       * 設定は存在するがOFFになっている
       */
      return {
        status: FuncStatus.SUCCESS,
        message: `This was no error, but user ${this.userId} doesn't turn on`,
      };
    } else {
      /* 特に問題ないので次へ */
    }

    const setting = ret.data as AmazonSubscribeSetting;

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

    const queryAfter = isEmulator ? 1 : convertUnixMillisecToSec(startTime);
    const queryBefore = convertUnixMillisecToSec(endTime);
    const queryRet = getAmazonNextShipNotifyMailIds(
      gmailClient,
      queryAfter,
      queryBefore
    );
    return {
      status: FuncStatus.SUCCESS,
    };
  }
}
