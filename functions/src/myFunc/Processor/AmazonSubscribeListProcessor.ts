import { logger } from "firebase-functions";
import { FuncResult, FuncStatus } from "../../type/FuncStatus";
import { createAmazonSubscribeSettingInstance } from "../../type/Mailbox";
import { MailboxExtractionService } from "../RealtimeDbService/MailboxExtractionService";

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

    const setting = ret.data;

    return {
      status: FuncStatus.SUCCESS,
    };
  }
}
