import { logger } from "firebase-functions";
import { gmail_v1 } from "googleapis";
import { GeneratedType } from "../../constants/GeneratedType";
import { Category } from "../../type/Category";
import { Expense } from "../../type/Expense";
import {
  FuncResult,
  FuncResultWithData,
  FuncStatus,
} from "../../type/FuncStatus";
import { BaseGoogleOAuthConfig } from "../../type/GoogleOAuthSecrets";
import {
  AllMailType,
  createRakutenPaySettingInstance,
  RakutenPaySetting,
} from "../../type/Mailbox";
import { GmailApiClient } from "../Client/GmailApiClient";
import { CategoryService } from "../FirestoreService/CategoryService";
import { ExpenseService } from "../FirestoreService/ExpenseService";
import { loadGoogleOAuthSecrets } from "../googleOAuthSecrets";
import { RakutenPayMailParser } from "../Parser/RakutenPayMailParser";
import { MailboxExtractionService } from "../RealtimeDbService/MailboxExtractionService";
import { extractTextBody } from "../utility/extractHtmlBody";
import {
import { Category }import { getCurrentUnixMillisec } from "../utility/getCurrentUnixSec";
 from './../../type/Category';
  convertUnixMillisecToSec,
  getCurrentUnixMillisec,
} from "../utility/getCurrentUnixSec";

/**
 * 各ユーザーに対してインスタンスを生成することにする！
 */
export class MailboxExtractionProcessor {
  private userId: string;

  constructor(
    userId: string,
    private mailboxExtractionService: MailboxExtractionService,
    private expenseService: ExpenseService,
    private categoryService: CategoryService
  ) {
    this.userId = userId;
  }

  async generateGmailApiInstance(
    userId: string
  ): Promise<FuncResultWithData<GmailApiClient>> {
    /**
     * Google認証に必要な情報+暗号化キーをロードする
     */
    const secretsRet = await loadGoogleOAuthSecrets();
    if (secretsRet.status != FuncStatus.SUCCESS) {
      return {
        status: secretsRet.status,
        message: `rakutenPayProcess: ${secretsRet.message}`,
      };
    }

    if (!secretsRet.data) {
      return {
        status: FuncStatus.ERROR,
        message: "OAuthSecrets was done, but data was empty",
      };
    }

    const oauthSecrets = secretsRet.data;
    const encryptionKey: string = oauthSecrets.encryptionKey;

    /**
     * RealtimeDBにrefreshTokenがあるかチェックする
     * なければ、そこで終了(楽天pay設定)
     */
    const tokenRet =
      await this.mailboxExtractionService.getMailboxExtractionTokenWithDecryption(
        userId,
        encryptionKey
      );

    if (tokenRet.status == FuncStatus.EMPTY) {
      /* まだユーザーがGmailのトークンの設定をしていない */
      return {
        status: FuncStatus.SUCCESS,
        message: "Rakuten Pay settting is not set by the user",
      };
    } else if (tokenRet.status != FuncStatus.SUCCESS) {
      return {
        status: tokenRet.status,
        message: tokenRet.message,
      };
    } else {
      /* do nothing */
    }

    if (!tokenRet.data) {
      return {
        status: FuncStatus.ERROR,
        message: "token was taken from Realtime Database, but data was empty",
      };
    }
    const refreshToken = tokenRet.data?.refreshToken;
    /* GmailApiClientを生成するのに必要なconfig */
    const gmailConfig: BaseGoogleOAuthConfig = {
      clientId: oauthSecrets.clientId,
      clientSecret: oauthSecrets.clientSecret,
      refreshToken: refreshToken,
    };

    logger.debug(
      `Gmail Config:${gmailConfig.clientId} ${gmailConfig.clientSecret} ${gmailConfig.refreshToken}`
    );

    /**
     * configをもとにGmailApiClientのインスタンス作成
     */
    const gmailApi = new GmailApiClient(gmailConfig);

    return {
      status: FuncStatus.SUCCESS,
      message: "Gmail Api Client was generated.",
      data: gmailApi,
    };
  }

  async getRakutenPayMailIds(
    gmailClient: GmailApiClient,
    startTime: number /* 時間で絞るための開始時刻(秒:整数) */,
    endTime: number /* 時間で絞るための終了時刻(秒:整数) */
  ): Promise<FuncResultWithData<string[]>> {
    /* まずはクエリをして楽天Payを抽出する */
    const subjectIncluded = "楽天ペイアプリご利用内容確認メール";

    /**
     * gmailのクエリは秒数+1~秒数-1でクエリがかかるらしい。
     * したがって、endTimeに+1をしてendTimeも含めるようにする
     * ちょっとここらへんが怖いな、
     */
    const endTimeAdded = endTime + 1;
    const query = `subject:${subjectIncluded} after:${startTime} before:${endTimeAdded}`;
    logger.debug(`Query:${query}`);
    const funcResult = await gmailClient.queryMessages(query);
    return funcResult;
  }

  /**
   * Expenseに対して保管して保存する
   */
  async addExpenseFromMailExtraction(
    baseExpense: Expense,
    type: AllMailType
  ): Promise<FuncResult> {
    const generatedType = `${GeneratedType.MAIL_EXTRACTION}___${type.nodeName}`;
    const timestamp = Date.now();

    const newExpense: Expense = {
      ...baseExpense,
      generatedType: generatedType,
      timestamp: timestamp,
    };

    const ret = await this.expenseService.addExpenseWithId(
      this.userId,
      baseExpense
    );
    return ret;
  }

  async processSingleMailType(type:AllMailType,categories:Record<string,Category>){
    const nodeName=type.nodeName;
    let ret=await this.mailboxExtractionService.getMailboxExtractionMailTypeSetting(this.userId,type);
    if(ret.status == FuncStatus.EMPTY){      
      /**
       * まだユーザーが設定していないのでやらない
       */
      return;
    }else if(
      ret.status!=FuncStatus.SUCCESS ||
      !ret.data
    ){      
      /**
       * なにかエラーが出たようだ
       */
      return;
    }else if(
      ret.data?.enabled == false
    ){
      /**
       * 設定は存在するが、OFFになっている
       */
      return;
    }else{
      /* 問題なさそうなので次へ */
    }

    /**
     * ここまで来れたら、直帰の実行状況を確認しに行く
     * RealtimeDatabaseのlastExecを取ってくる。
     * 取ってきたら
     */
    const lastExecRet =
      await this.mailboxExtractionService.getMailboxExtractionLastExec(
        this.userId,
        type
      );

    if (lastExecRet.status != FuncStatus.SUCCESS) {
      logger.info(`${lastExecRet.message}`);
      return;
    }

    const endTime = getCurrentUnixMillisec();
    const lastMsgId = lastExecRet.data?.lastMsgId; /* nullの可能性もある */
    

  };

  /**
   * 最終的には、どのメールに対しても似たような操作をすることになる。
   * これ単体で実行することはほとんどないはず。
   */
  async processRakutenPayMails(categories: Record<string, Category> = {}) {
    /**
     * まずそもそも楽天Payの設定をユーザーがしているかチェック
     */
    const rakutenPaySample: RakutenPaySetting = createRakutenPaySettingInstance(
      {
        enabled: true,
      }
    );
    const rakutenPayRet =
      await this.mailboxExtractionService.getRakutenPaySetting(this.userId);

    if (rakutenPayRet.status == FuncStatus.EMPTY) {
      logger.info("user didn't turn on RakutenPay setting yet.");
      return;
    } else if (
      rakutenPayRet.status != FuncStatus.SUCCESS ||
      rakutenPayRet.data === null
    ) {
      logger.info(`${rakutenPayRet.message}`);
      return;
    } else if (rakutenPayRet.data?.enabled == false) {
      /* 設定は存在するがOFFにしているので、行わない */
      logger.info(`The user doesn't set Rakutenpay enabled.`);
      return;
    }

    /**
     * RealtimeDatabaseのlastExecを取ってくる。
     * 取ってきたら
     */
    const lastExecRet =
      await this.mailboxExtractionService.getMailboxExtractionLastExec(
        this.userId,
        rakutenPaySample
      );

    if (lastExecRet.status != FuncStatus.SUCCESS) {
      logger.info(`${lastExecRet.message}`);
      return;
    }

    /* 開始時刻と終了時刻を設定 */
    const endTime = getCurrentUnixMillisec();
    const lastMsgId = lastExecRet.data?.lastMsgId; /* nullの可能性もある */
    let startTime: number = 0;
    if (!lastExecRet.data?.timestamp) {
      /* timestampがない場合 */
      startTime = endTime - 60 * 5 * 1000; /* 5分前の時間を開始時刻とする */
    } else {
      startTime =
        lastExecRet.data
          .timestamp; /* タイムスタンプがすでにあるならそれを使う */
    }

    /**
     * GmailApiを取得してくる
     */
    const gmailClientRet = await this.generateGmailApiInstance(this.userId);
    if (gmailClientRet.status != FuncStatus.SUCCESS || !gmailClientRet.data) {
      logger.info(`${gmailClientRet.message}`);
      return;
    }
    const gmailCliet: GmailApiClient = gmailClientRet.data;

    /**
     * クエリをして、msgIdを取得
     */
    /*　デバッグのため、Afterだけ書き換える!! */
    const akitaDebug = true;
    let queryAfter: number = 0;
    if (akitaDebug) {
      queryAfter = 1;
    }

    /*const queryAfter = convertUnixMillisecToSec(startTime);//本番はこっち */
    const queryBefore = convertUnixMillisecToSec(endTime);
    const queryRet = await this.getRakutenPayMailIds(
      gmailCliet,
      queryAfter,
      queryBefore
    );
    if (queryRet.status != FuncStatus.SUCCESS) {
      /**
       * クエリに事故っているならタイムスタンプは更新しない方が良い
       * ここで更新してしまうと今回の実行時と次の実行時の間のメッセージがスキップされてしまう
       */
      logger.error(`${queryRet.message}`);
      return;
    }

    if (!queryRet.data || queryRet.data.length === 0) {
      /**
       * 何もヒットしなかった
       */
      logger.info("Nothing was found After query.");

      await this.mailboxExtractionService.setMailboxExtractionLastExec(
        this.userId,
        rakutenPaySample,
        {
          timestamp: endTime /* UNIXミリ秒で保存 */,
          ...lastExecRet.data,
        }
      );
    } else {
      /**
       * クエリでなにかしらヒットした
       */
      const messageMap: Record<string, gmail_v1.Schema$Message | undefined> =
        {}; /* ここにデータをいれていく */
      const hitMsgIds = queryRet.data;
      logger.info(`Found mails ${queryRet.data.length}`);

      for (const id of hitMsgIds) {
        console.log("\n\n---------------------------------\n");
        const res = await gmailCliet.getMessageDetail(id);
        messageMap[id] = res.data;
        const rawText = extractTextBody(messageMap[id]?.payload);
        if (!rawText) {
          logger.error("Failed to extract Text Body.");
        } else {
          /* テキストをちゃんと取れた */
          const parser = new RakutenPayMailParser(rawText);
          const result = parser.toExpense();
          if (result.status != FuncStatus.SUCCESS) {
            logger.error(`msgid:${id} failed! ${result.message}`);
            continue;
          }
        }
      }
    }
  }
}
