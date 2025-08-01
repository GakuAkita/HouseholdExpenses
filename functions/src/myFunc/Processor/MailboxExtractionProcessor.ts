import { logger } from "firebase-functions";
import { gmail_v1 } from "googleapis";
import { GeneratedType } from "../../constants/GeneratedType";
import { Category } from "../../type/Category";
import { CategoryAssignmentData } from "../../type/CategoryAssignment";
import { Expense } from "../../type/Expense";
import {
  FuncResult,
  FuncResultWithData,
  FuncStatus,
} from "../../type/FuncStatus";
import { BaseGoogleOAuthConfig } from "../../type/GoogleOAuthSecrets";
import {
  AllMailType,
  allMailTypeList,
  AmazonItemSetting,
  AmazonKindleSetting,
  createAmazonItemSettingInstance,
  createAmazonKindleSettingInstance,
  createRakutenPaySettingInstance,
  createShikokuElectricPowerSettingInstance,
  LastMailboxExtractionExec,
  RakutenPaySetting,
  ShikokuElectricPowerSetting,
} from "../../type/Mailbox";
import { GmailApiClient } from "../Client/GmailApiClient";
import { CategoryService } from "../FirestoreService/CategoryService";
import { ExpenseService } from "../FirestoreService/ExpenseService";
import { loadGoogleOAuthSecrets } from "../googleOAuthSecrets";
import { AmazonItemParser } from "../Parser/AmazonItemParser";
import { AmazonKindleMailParser } from "../Parser/AmazonKindleMailParser";
import { RakutenPayMailParser } from "../Parser/RakutenPayMailParser";
import { ShikokuElectricPowerMailParser } from "../Parser/ShikokuElectricPowerMailParser";
import { CategoryAssignmentService } from "../RealtimeDbService/CategoryAssignmentService";
import { MailboxExtractionService } from "../RealtimeDbService/MailboxExtractionService";
import { categoryAssign } from "../utility/cateogryAssign";
import {
  convertUnixMillisecToSec,
  getCurrentUnixMillisec,
} from "../utility/getCurrentUnixSec";
import { extractTextBody } from "../utility/gmail/extractHtmlBody";
import { getInternalDateMillisFromMessage } from "../utility/gmail/getInternalDate";
/**
 * 各ユーザーに対してインスタンスを生成することにする！
 */
export class MailboxExtractionProcessor {
  private userId: string;
  private categories: Record<string, Category> | null = null;
  private categoryAssignmentData: CategoryAssignmentData | null =
    null; /* 今のところ毎回全部取るが、将来的に商品名か店名の片方で良いかも */

  constructor(
    userId: string,
    private mailboxExtractionService: MailboxExtractionService,
    private expenseService: ExpenseService,
    private categoryService: CategoryService,
    private categoryAssignmentService: CategoryAssignmentService
  ) {
    this.userId = userId;
  }

  /* ***************カテゴリーの読み込み************************ */
  private async loadCategories(): Promise<
    FuncResultWithData<Record<string, Category>>
  > {
    /**
     * 各インスタンス1個に対して1回実行。
     * mailidが見つかったときしか実行されないので、読み取り回数について気にする必要はあまりない。
     *  */
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

  /* ************************カテゴリー割当の読み込み***************************** */
  private async loadCategoryAssignmentData(): Promise<
    FuncResultWithData<CategoryAssignmentData>
  > {
    if (this.categoryAssignmentData === null) {
      const result =
        await this.categoryAssignmentService.getCategoryAssignmentData(
          this.userId
        );
      if (result.status === FuncStatus.EMPTY) {
        this.categoryAssignmentData = { storeName: {}, productName: {} };
        return {
          status: FuncStatus.SUCCESS,
          message:
            "There was no error in getCategoryAssignmentData, but empty.",
          data: this.categoryAssignmentData,
        };
      }
      if (result.status !== FuncStatus.SUCCESS) {
        return result;
      }

      if (!result.data) {
        /* カテゴリー割当がない可能性もあるから、成功として扱う。 */
        this.categoryAssignmentData = {
          storeName: {},
          productName: {},
        };
        return {
          status: FuncStatus.SUCCESS,
          message:
            "There was no error in getCategoryAssignmentData, but empty.",
        };
      }
      this.categoryAssignmentData = result.data;
    }

    return {
      status: FuncStatus.SUCCESS,
      message: "Already loaded before.",
      data: this.categoryAssignmentData,
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
      /**
       *  7 PERMISSION_DENIED: Permission 'secretmanager.versions.access' denied for resource .....
       * こちらのエラーが出た場合は、compute....にSecret ManagerのSecret Accessorの権限を付与する必要がある。
       * */
      return {
        status: secretsRet.status,
        message: `generateGmailApiInstance: ${secretsRet.message}`,
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
      await this.mailboxExtractionService.getMailboxExtractionGmailTokenWithDecryption(
        this.userId,
        encryptionKey
      );

    if (tokenRet.status == FuncStatus.EMPTY) {
      /* まだユーザーがGmailのトークンの設定をしていない */
      return {
        status: FuncStatus.SUCCESS,
        message: "Gmail Token is not set by the user",
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
    const shikokuElectricSamp = createShikokuElectricPowerSettingInstance();
    const amazonItemSamp = createAmazonItemSettingInstance();

    let ret: FuncResultWithData<string[]>;
    switch (nodeName) {
      /**
       * クエリの文章だけ定義して、
       * この関数内でqueryしてもいいかもな。
       */
      case rakutenPaySamp.nodeName:
        ret = await this.getRakutenPayMailIds(gmailClient, startTime, endTime);
        break;

      case amazonKindleSamp.nodeName:
        /* 特に何もやらない */
        ret = await this.getAmazonKindleMailIds(
          gmailClient,
          startTime,
          endTime
        );
        break;

      case shikokuElectricSamp.nodeName:
        ret = await this.getShikokuElectricMailIds(
          gmailClient,
          startTime,
          endTime
        );
        break;

      case amazonItemSamp.nodeName:
        ret = await this.getAmazonItemMailIds(gmailClient, startTime, endTime);
        break;

      default:
        ret = {
          status: FuncStatus.ERROR,
          message: `Unknown type:${nodeName}`,
        };
        break;
    }
    return ret;
  }

  async getRakutenPayMailIds(
    gmailClient: GmailApiClient,
    startTime: number /* 時間で絞るための開始時刻(秒:整数) */,
    endTime: number /* 時間で絞るための終了時刻(秒:整数) */
  ): Promise<FuncResultWithData<string[]>> {
    /* まずはクエリをして楽天Payを抽出する */
    const subjectIncluded = "楽天ペイアプリご利用内容確認メール";
    const mailFrom = "no-reply@pay.rakuten.co.jp";

    /**
     * gmailのクエリは秒数+1~秒数-1でクエリがかかるらしい。
     * したがって、endTimeに+1をしてendTimeも含めるようにする
     * ちょっとここらへんが怖いな、
     */
    const endTimeAdded = endTime + 1;
    const query = `subject:${subjectIncluded} from:${mailFrom} after:${startTime} before:${endTimeAdded}`;
    logger.debug(`Query:${query}`);
    const funcResult = await gmailClient.queryMessages(query);
    return funcResult;
  }

  async getAmazonKindleMailIds(
    gmailClient: GmailApiClient,
    startTime: number,
    endTime: number
  ): Promise<FuncResultWithData<string[]>> {
    const mailFrom = "digital-no-reply@amazon.co.jp";
    const wordIncluded =
      "Kindle"; /* まあこれなくてもいいけど、、一応つけておく。本文または件名に含まれる */

    const endTimeAdded = endTime + 1;
    const query = `from:${mailFrom} ${wordIncluded} after:${startTime} before:${endTimeAdded}`;
    logger.debug(`Query:${query}`);
    const funcResult = await gmailClient.queryMessages(query);
    return funcResult;
  }

  async getShikokuElectricMailIds(
    gmailClient: GmailApiClient,
    startTime: number,
    endTime: number
  ): Promise<FuncResultWithData<string[]>> {
    const mailFrom = "yonden-con@yonden.co.jp";
    const wordIncluded = "【四国電力】電気料金等のお知らせ";
    const endTimeAdded = endTime + 1;
    const query = `from:${mailFrom} ${wordIncluded} after:${startTime} before:${endTimeAdded}`;
    logger.debug(`Query:${query}`);
    const funcResult = await gmailClient.queryMessages(query);
    return funcResult;
  }

  async getAmazonItemMailIds(
    gmailClient: GmailApiClient,
    startTime: number,
    endTime: number
  ): Promise<FuncResultWithData<string[]>> {
    const mailFrom = "auto-confirm@amazon.co.jp";
    const endTimeAdded = endTime + 1;
    const query = `from:${mailFrom} after:${startTime} before:${endTimeAdded}`;
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
      newExpense
    );
    return ret;
  }

  /**
   * メールの本文からデータを抽出して
   * Expenseの保存まで行う
   */
  async saveExpenseWithExtraction(
    setting: AllMailType,
    rawText: string,
    sentDate?: string | null
  ): Promise<FuncResult> {
    const nodeName = setting.nodeName;
    const rakutenPaySamp = createRakutenPaySettingInstance();
    const amazonKindleSamp = createAmazonKindleSettingInstance();
    const shikokuElectricSamp = createShikokuElectricPowerSettingInstance();
    const amazonItemSamp = createAmazonItemSettingInstance();

    let categories: Record<string, Category> = {};
    const categoryRet = await this.loadCategories();
    if (categoryRet.status != FuncStatus.SUCCESS) {
      /* カテゴリーの読み込み失敗の場合はログ表示だけにしておく */
      logger.error(`Failed to load categories: ${categoryRet.message}`);
    } else if (categoryRet.data) {
      categories = categoryRet.data;
    } else {
      /* Do nothing */
    }

    let categoryAssignmentData: CategoryAssignmentData = {
      storeName: {},
      productName: {},
    };
    const assignRet = await this.loadCategoryAssignmentData();
    if (assignRet.status != FuncStatus.SUCCESS) {
      /* カテゴリー割当の読み込み失敗の場合はログ表示だけにしておく */
      logger.error(
        `Failed to load category assignment data: ${assignRet.message}`
      );
    } else if (assignRet.data) {
      categoryAssignmentData = assignRet.data;
    } else {
      /* Do nothing */
      /* ここに来ることはあまりないのでは？ */
    }

    let ret: FuncResult = {
      status: FuncStatus.SUCCESS,
      message: "Success",
    };
    switch (nodeName) {
      case rakutenPaySamp.nodeName:
        ret = await this.saveExpenseFromRakutenPay(
          rawText,
          setting,
          categories,
          categoryAssignmentData
        );
        break;

      case amazonKindleSamp.nodeName:
        ret = await this.saveExpenseFromAmazonKindle(
          rawText,
          setting,
          categories
        );
        break;

      case shikokuElectricSamp.nodeName:
        ret = await this.saveExpenseFromShikokuElectricPower(
          rawText,
          setting,
          categories,
          sentDate
        );
        break;

      case amazonItemSamp.nodeName:
        ret = await this.saveExpenseFromAmazonItem(
          rawText,
          setting,
          categories,
          categoryAssignmentData,
          sentDate
        );
        break;

      default:
        ret = {
          status: FuncStatus.ERROR,
          message: `Not prepared type for MailboxExtraction: ${nodeName}`,
        };
        logger.error(`Not prepared type for MailboxExtraction: ${nodeName}`);
        break;
    }

    return ret;
  }

  async saveExpenseFromRakutenPay(
    rawText: string,
    setting: RakutenPaySetting,
    categories: Record<string, Category>,
    assignmentData: CategoryAssignmentData
  ): Promise<FuncResult> {
    const parser = new RakutenPayMailParser(rawText);
    const ret = parser.toExpense(); /* この時点では最低限しかいれていない */
    if (ret.status != FuncStatus.SUCCESS || !ret.data) {
      return ret;
    }

    const baseExpense: Expense = ret.data;
    if (assignmentData.storeName && baseExpense.storeName) {
      const category = categoryAssign(
        baseExpense.storeName,
        assignmentData.storeName,
        categories
      );

      /**
       * カテゴリーがヒットしたら更新
       */
      if (category) {
        baseExpense.category = category;
      }
    }

    const addRet = await this.addExpenseFromMailExtraction(
      baseExpense,
      setting
    );

    return addRet;
  }

  async saveExpenseFromAmazonKindle(
    rawText: string,
    setting: AmazonKindleSetting,
    categories: Record<string, Category>
  ): Promise<FuncResult> {
    const parser = new AmazonKindleMailParser(rawText);
    const ret = parser.toExpense();
    if (ret.status != FuncStatus.SUCCESS || !ret.data) {
      return ret;
    }

    const baseExpense: Expense = ret.data;
    if (setting.categoryId) {
      baseExpense.category = categories[setting.categoryId];
    }

    const addRet = await this.addExpenseFromMailExtraction(
      baseExpense,
      setting
    );

    return addRet;
  }

  async saveExpenseFromShikokuElectricPower(
    rawText: string,
    setting: ShikokuElectricPowerSetting,
    categories: Record<string, Category>,
    internalDate?: string | null
  ): Promise<FuncResult> {
    if (!internalDate) {
      return {
        status: FuncStatus.ERROR,
        message: `when saving ShikokuElectricPower, internal Date should not be empty.`,
      };
    }

    const parser = new ShikokuElectricPowerMailParser(rawText, internalDate);
    const ret = parser.toExpense();
    if (ret.status != FuncStatus.SUCCESS || !ret.data) {
      return ret;
    }
    const baseExpense = ret.data;
    if (setting.categoryId) {
      baseExpense.category = categories[setting.categoryId];
    }
    const addRet = this.addExpenseFromMailExtraction(baseExpense, setting);

    return addRet;
  }

  async saveExpenseFromAmazonItem(
    rawText: string,
    setting: AmazonItemSetting,
    categories: Record<string, Category>,
    assignmentData: CategoryAssignmentData,
    internalDate?: string | null
  ): Promise<FuncResult> {
    if (!internalDate) {
      return {
        status: FuncStatus.ERROR,
        message: `when saving AmazonItem, internal Date should not be empty.`,
      };
    }
    const parser = new AmazonItemParser(rawText, internalDate);
    console.log(parser.extractDate());
    console.log(rawText);
    const expensesAdded = parser.toExpenses();

    /* 一個でもaddできたらtrueに戻す */
    let expenseAddedFlag = false;
    /* 配列にExpenseが入っているので全部ループする。 */
    for (const expense of expensesAdded) {
      /* 製品名でカテゴリー割当をする */
      if (assignmentData.productName && expense.itemName) {
        const category = categoryAssign(
          expense.itemName,
          assignmentData.productName,
          categories
        );

        /**
         * カテゴリーがヒットしたら更新
         */
        if (category) {
          expense.category = category;
        }
      }
      /* Firestoreに保存する */
      /**
       * 1個も保存できていなかったら、エラーを吐く
       * なぜならメールのフォーマットが変わって何も取得できなかったか、addができなかった可能性があるから。
       *  */
      const addRet = await this.addExpenseFromMailExtraction(expense, setting);
      if (addRet.status == FuncStatus.SUCCESS) {
        expenseAddedFlag = true;
      } else {
        logger.error(`error at saveExpenseFromAmazonItem:${addRet.message}`);
      }
    }

    if (expenseAddedFlag) {
      return {
        status: FuncStatus.SUCCESS,
        message: `at least one expense was added`,
      };
    } else {
      return {
        status: FuncStatus.ERROR,
        message: `No expense was added. Unable to extract any expenses`,
      };
    }
  }

  /* ******************************実際に呼び出す処理(全体)************************************* */
  /**
   * @todo
   * 基本Gmailだが将来的にOutlookとか増えた場合、ここの処理の変更が必要。
   * 一応、各メールテンプレートのdata classになんのメールで登録しているか持たせている。(今は全部Gmailだが)
   */
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
      logger.debug(`Skip ${type.nodeName}. ${ret.message}`);
      return;
    } else if (ret.status != FuncStatus.SUCCESS || !ret.data) {
      /**
       * なにかエラーが出たようだ
       */
      logger.error(
        `${type.nodeName} went wrong when getting setting.: ${ret.message}`
      );
      return;
    } else if (ret.data?.enabled == false) {
      /**
       * 設定は存在するが、OFFになっている
       */
      logger.debug(`Skip ${type.nodeName} Not Enabled.`);
      return;
    } else {
      /* 問題なさそうなので次へ */
    }

    const setting = ret.data;

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
    const isEmulator = process.env.FUNCTIONS_EMULATOR === "true";
    const queryAfter = isEmulator
      ? 1
      : convertUnixMillisecToSec(
          startTime
        ); /* emulatorの場合は1をいれて全部取ってくる */
    const queryBefore = convertUnixMillisecToSec(endTime);
    const queryRet = await this.getMailIdsByQuery(
      type,
      gmailCliet,
      queryAfter,
      queryBefore
    );

    if (!queryRet.data || queryRet.data.length === 0) {
      /**
       * 何もヒットしなかった
       */
      logger.info("Nothing was found After query.");
      const ret =
        await this.mailboxExtractionService.setMailboxExtractionLastExec(
          this.userId,
          type,
          {
            timestamp: endTime /* UNIXミリ秒で保存 */,
            ...lastExecRet.data,
          }
        );
      if (ret.status != FuncStatus.SUCCESS) {
        logger.error(`${ret.message}`);
      }
    } else {
      /**
       * クエリでなにかしらヒットした
       * まずはヒットしたすべてのIDを格納して、mapとして持っておく
       */
      const messageMap: Record<string, gmail_v1.Schema$Message> = {};
      const hitMsgIds = queryRet.data;
      logger.info(`Found mails ${queryRet.data.length}`);

      for (const id of hitMsgIds) {
        const res = await gmailCliet.getMessageDetail(id);
        if (res.status != FuncStatus.SUCCESS || !res.data) {
          logger.error(`getMessageDetail failed: id=${id} msg=${res.message}`);
          continue;
        }
        messageMap[id] = res.data;
      }
      /**
       * internalDate順(新しい順)に並び替えて
       * msgIdがlastMsgIdと一致したらそれより後ろの古いmsgIdは無視する
       */
      const sortedEntries = Object.entries(messageMap).sort((a, b) => {
        /**
         * 新しいinternalDateのメッセージを前に並べる
         * nullがあればそれは一番うしろに回す
         */
        const dateA = getInternalDateMillisFromMessage(a[1]);
        const dateB = getInternalDateMillisFromMessage(b[1]);

        // null 安全性を確保：null は最も古いとみなす（＝最後に来る）
        if (dateA === null && dateB === null) return 0;
        if (dateA === null) return 1;
        if (dateB === null) return -1;

        return dateB - dateA; // 降順（新しい順）
      });
      const filteredMessages: Record<string, gmail_v1.Schema$Message> = {};
      let mostRecentMsgId: string | null =
        null; /* 最後にLastExecを更新するときに使う */
      for (const [id, message] of sortedEntries) {
        if (id === lastMsgId) {
          logger.info(`Found lastMsgId again.${id}`);
          break; // これより古いメッセージは無視
        }

        if (!mostRecentMsgId) {
          mostRecentMsgId = id; /* 最後にいれたメッセージが一番新しい */
        }
        filteredMessages[id] = message;
      }

      /* filterdMessagesに対してすべてExpense保存まで行う */
      for (const [_, message] of Object.entries(filteredMessages)) {
        const rawText = extractTextBody(message.payload);
        const internalDate = message.internalDate;
        if (!rawText) {
          logger.error("Failed to extract Text Body.");
        } else {
          /**
           * 関数内でExpenseの保存まで済ませてしまう
           */
          const ret = await this.saveExpenseWithExtraction(
            setting,
            rawText,
            internalDate /* メールによっては日時が本文内にないケースが有る */
          );
          if (ret.status != FuncStatus.SUCCESS) {
            logger.error(`${ret.message}`);
          }
          /* 失敗しようが何しようが次に行く */
        }
      }

      /* 最後にlastExecを更新する */
      const lastExec: LastMailboxExtractionExec = {
        timestamp: endTime,
        lastMsgId: mostRecentMsgId,
      };
      const ret =
        await this.mailboxExtractionService.setMailboxExtractionLastExec(
          this.userId,
          type,
          lastExec
        );
      if (ret.status != FuncStatus.SUCCESS) {
        logger.error(`${ret.message}`);
      }
    }
  }

  /**
   * すべてのメールタイプに対して、実行する
   */
  async processAllMailType() {
    for (const type of allMailTypeList) {
      await this.processSingleMailType(type);
    }
  }
}
