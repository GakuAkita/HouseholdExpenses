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
  createAmazonKindleSettingInstance,
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
  convertUnixMillisecToSec,
  getCurrentUnixMillisec,
} from "../utility/getCurrentUnixSec";
/**
 * 各ユーザーに対してインスタンスを生成することにする！
 */
export class MailboxExtractionProcessor {
  private userId: string;
  private categories: Record<string, Category> | null = null;

  constructor(
    userId: string,
    private mailboxExtractionService: MailboxExtractionService,
    private expenseService: ExpenseService,
    private categoryService: CategoryService
  ) {
    this.userId = userId;
  }

  /* ***************カテゴリーの読み込み************************ */
  private async loadCategories(): Promise<
    FuncResultWithData<Record<string, Category>>
  > {
    if (this.categories === null) {
      const result = await this.categoryService.getAllCategories(this.userId);
      if (result.status !== FuncStatus.SUCCESS) {
        return result;
        /* 下は実行されないからcategoriesはnullのまま */
      }

      if (!result.data) {
        /* カテゴリーがない可能性もあるから、成功として扱う。 */
        this.categories = {};
        return {
          status: FuncStatus.SUCCESS,
          message: "There was no error in getAllCateogries, but empty.",
        };
      }
      this.categories = result.data;
    }

    return {
      status: FuncStatus.SUCCESS,
      message: "Already loaded before.",
      data: this.categories,
    };
  }

  /* ************************GmailApiClientの生成***************************** */
  async generateGmailApiInstance(): Promise<
    FuncResultWithData<GmailApiClient>
  > {
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
        this.userId,
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

  /* *****************************Gmailのクエリ関係************************************ */
  async getMailIdsByQuery(
    type: AllMailType,
    gmailClient: GmailApiClient,
    startTime: number,
    endTime: number
  ): Promise<FuncResultWithData<string[]>> {
    const nodeName = type.nodeName;

    const rakutenPaySamp = createRakutenPaySettingInstance();
    const amazonKindleSamp = createAmazonKindleSettingInstance();

    switch (nodeName) {
      /**
       * クエリの文章だけ定義して、
       * この関数内でqueryしてもいいかもな。
       */
      case rakutenPaySamp.nodeName:
        return await this.getRakutenPayMailIds(gmailClient, startTime, endTime);
        break;

      case amazonKindleSamp.nodeName:
        /* 特に何もやらない */
        return {
          status: FuncStatus.SUCCESS,
          message: "Not prepared.",
          data: [],
        };
        break;

      default:
        return {
          status: FuncStatus.ERROR,
          message: `Unknown type:${nodeName}`,
        };
        break;
    }
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

  /* ***************************抽出したテキストparseしてExpenseを保存************************************** */
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

  /**
   * メールの本文からデータを抽出して
   * Expenseの保存まで行う
   */
  async saveExpenseWithExtraction(
    type: AllMailType,
    rawText: string
  ): Promise<FuncResult> {
    const nodeName = type.nodeName;
    const rakutenPaySamp = createRakutenPaySettingInstance();
    const amazonKindleSamp = createAmazonKindleSettingInstance();

    let categories: Record<string, Category> = {};
    const categoryRet = await this.loadCategories();
    if (categoryRet.status != FuncStatus.SUCCESS) {
      /* カテゴリーの読み込み失敗の場合はログ表示だけにしておく */
    } else if (categoryRet.data) {
      categories = categoryRet.data;
    } else {
      /* Do nothing */
    }

    switch (nodeName) {
      case rakutenPaySamp.nodeName:
        break;

      case amazonKindleSamp.nodeName:
        break;

      default:
        break;
    }
  }

  async saveExpenseFromRakutenPay(
    rawText: string,
    setting: RakutenPaySetting,
    categories: Record<string, Category>
  ): Promise<FuncResult> {
    const parser = new RakutenPayMailParser(rawText);
    const ret = parser.toExpense(); /* この時点では最低限しかいれていない */
    if (ret.status != FuncStatus.SUCCESS || !ret.data) {
      return ret;
    }

    const baseExpense: Expense = ret.data;

    /**
     * とりあえずは完全一致の場合しか受け付けないが、
     * 将来的には部分一致でもカテゴリーをつけられるようにしたい
     * 例えば、ローソンだったらどの店舗だろうが消費につけるとか。
     */
    if (setting.storeCategoryAssignments && baseExpense.storeName) {
      const categoryId = setting.storeCategoryAssignments[baseExpense.storeName];
    }
  }

  /* ******************************実際に呼び出す処理(全体)************************************* */
  async processSingleMailType(type: AllMailType) {
    const nodeName = type.nodeName;
    let ret =
      await this.mailboxExtractionService.getMailboxExtractionMailTypeSetting(
        this.userId,
        type
      );
    if (ret.status == FuncStatus.EMPTY) {
      /**
       * まだユーザーが設定していないのでやらない
       */
      return;
    } else if (ret.status != FuncStatus.SUCCESS || !ret.data) {
      /**
       * なにかエラーが出たようだ
       */
      return;
    } else if (ret.data?.enabled == false) {
      /**
       * 設定は存在するが、OFFになっている
       */
      return;
    } else {
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

    /* データベースにもミリ秒で保存する */
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
    const gmailClientRet = await this.generateGmailApiInstance();
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
    const queryRet = await this.getMailIdsByQuery(
      type,
      gmailCliet,
      startTime,
      endTime
    );

    if (!queryRet.data || queryRet.data.length === 0) {
      /**
       * 何もヒットしなかった
       */
      logger.info("Nothing was found After query.");

      await this.mailboxExtractionService.setMailboxExtractionLastExec(
        this.userId,
        type,
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
        console.log(
          "\n\n---------------------------------\n"
        ); /* デバッグ終わったら消す、、 */
        const res = await gmailCliet.getMessageDetail(id);
        messageMap[id] = res.data;
        const rawText = extractTextBody(messageMap[id]?.payload);
        if (!rawText) {
          logger.error("Failed to extract Text Body.");
        } else {
          /**
           * 関数内でExpenseの保存まで済ませてしまう
           */
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
