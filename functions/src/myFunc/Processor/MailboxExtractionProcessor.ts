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
import {
  AllMailType,
  AmazonItemSetting,
  AmazonKindleSetting,
  createAmazonItemSettingInstance,
  createAmazonKindleSettingInstance,
  createRakutenCardETCSettingInstance,
  createRakutenPaySettingInstance,
  createShikokuElectricPowerSettingInstance,
  createUdemySettingInstance,
  LastMailboxExtractionExec,
  RakutenCardETCSetting,
  RakutenPaySetting,
  ShikokuElectricPowerSetting,
  UdemySetting,
} from "../../type/Mailbox";
import { GmailApiClient } from "../Client/GmailApiClient";
import { CategoryService } from "../FirestoreService/CategoryService";
import { ExpenseService } from "../FirestoreService/ExpenseService";
import { AmazonItemMailParser } from "../Parser/AmazonItemMailParser";
import { AmazonKindleMailParser } from "../Parser/AmazonKindleMailParser";
import { RakutenCardETCParser } from "../Parser/RakutenCardETCParser";
import { RakutenPayMailParser } from "../Parser/RakutenPayMailParser";
import { ShikokuElectricPowerMailParser } from "../Parser/ShikokuElectricPowerMailParser";
import { UdemyMailParser } from "../Parser/UdemyMailParser";
import { CategoryAssignmentService } from "../RealtimeDbService/CategoryAssignmentService";
import { MailboxExtractionService } from "../RealtimeDbService/MailboxExtractionService";
import {
  assignCategoryById,
  assignCategoryFromAssignmentData,
} from "../utility/cateogryAssign";
import {
  convertUnixMillisecToSec,
  getCurrentUnixMillisec,
} from "../utility/getCurrentUnixSec";
import { extractTextBody } from "../utility/gmail/extractHtmlBody";
import { generateGmailApiInstance } from "../utility/gmail/generateGmailApiInstance";
import { getMessageDetailsSortedList } from "../utility/gmail/getMessageDetailsMap";
import {
  getAmazonItemMailIds,
  getAmazonKindleMailIds,
  getRakutenCardETCMailIds,
  getRakutenPayMailIds,
  getShikokuElectricMailIds,
  getUdemyMailIds,
} from "../utility/gmail/mailQueries";
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
    const udemySetting = createUdemySettingInstance();
    const rakutenETCSamp = createRakutenCardETCSettingInstance();

    let ret: FuncResultWithData<string[]>;
    switch (nodeName) {
      /**
       * クエリの文章だけ定義して、
       * この関数内でqueryしてもいいかもな。
       */
      case rakutenPaySamp.nodeName:
        ret = await getRakutenPayMailIds(gmailClient, startTime, endTime);
        break;

      case amazonKindleSamp.nodeName:
        /* 特に何もやらない */
        ret = await getAmazonKindleMailIds(gmailClient, startTime, endTime);
        break;

      case shikokuElectricSamp.nodeName:
        ret = await getShikokuElectricMailIds(gmailClient, startTime, endTime);
        break;

      case amazonItemSamp.nodeName:
        ret = await getAmazonItemMailIds(gmailClient, startTime, endTime);
        break;

      case udemySetting.nodeName:
        ret = await getUdemyMailIds(gmailClient, startTime, endTime);
        break;

      case rakutenETCSamp.nodeName:
        ret = await getRakutenCardETCMailIds(gmailClient, startTime, endTime);
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
    const udemySamp = createUdemySettingInstance();
    const rakutenETCSamp = createRakutenCardETCSettingInstance();

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
          categories,
          sentDate
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

      case udemySamp.nodeName:
        ret = await this.saveExpenseFromUdemmy(
          rawText,
          setting,
          categories,
          sentDate
        );
        break;

      case rakutenETCSamp.nodeName:
        ret = await this.saveExpenseFromRakutenCardETC(
          rawText,
          setting,
          categories
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
    /**
     * baseExpenseに店名が入っていて
     * かつ、
     * ユーザーが登録した店名のカテゴリー割当データが存在すれば
     * 見つけてカテゴリー割当をする。(ヒットしない可能性もあるが)
     */
    const expenseWithCategory =
      assignmentData.storeName && baseExpense.storeName
        ? assignCategoryFromAssignmentData(
            baseExpense,
            baseExpense.storeName,
            assignmentData.storeName,
            categories
          )
        : baseExpense;

    const addRet = await this.addExpenseFromMailExtraction(
      expenseWithCategory,
      setting
    );

    return addRet;
  }

  async saveExpenseFromAmazonKindle(
    rawText: string,
    setting: AmazonKindleSetting,
    categories: Record<string, Category>,
    internalDate?: string | null
  ): Promise<FuncResult> {
    if (!internalDate) {
      return {
        status: FuncStatus.ERROR,
        message: `when sving AmazonKindle, internalDate should not be empty.`,
      };
    }

    const parser = new AmazonKindleMailParser(rawText, internalDate);
    const ret = parser.toExpense();
    if (ret.status != FuncStatus.SUCCESS || !ret.data) {
      return ret;
    }

    const baseExpense: Expense = ret.data;
    const expenseWithCategory = assignCategoryById(
      baseExpense,
      setting.categoryId /* カテゴリーidが何もなければ無割当で返ってくる。それも関数内でやっている */,
      categories
    );

    const addRet = await this.addExpenseFromMailExtraction(
      expenseWithCategory,
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
    const expenseWithCategory = assignCategoryById(
      baseExpense,
      setting.categoryId,
      categories
    );

    const addRet = this.addExpenseFromMailExtraction(
      expenseWithCategory,
      setting
    );

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
    const parser = new AmazonItemMailParser(rawText, internalDate);
    const parseRet = parser.toExpenses();
    if (parseRet.status != FuncStatus.SUCCESS) {
      return parseRet;
    } else if (!parseRet.data) {
      return {
        status: FuncStatus.ERROR,
        message:
          "saveExepnseFromAmazonItem : funcStatus was success, but data was not attached.",
      };
    } else {
      /* Do nothing */
    }

    const expensesAdded = parseRet.data;

    /* 一個でもaddできたらtrueに戻す */
    let expenseAddedFlag = false;
    /* 配列にExpenseが入っているので全部ループする。 */
    for (const expense of expensesAdded) {
      /* 製品名でカテゴリー割当をする */
      const expenseWithCategory =
        assignmentData.productName && expense.itemName
          ? assignCategoryFromAssignmentData(
              expense,
              expense.itemName,
              assignmentData.productName,
              categories
            )
          : expense;

      /* Firestoreに保存する */
      /**
       * 1個も保存できていなかったら、エラーを吐く
       * なぜならメールのフォーマットが変わって何も取得できなかったか、addができなかった可能性があるから。
       *  */
      const addRet = await this.addExpenseFromMailExtraction(
        expenseWithCategory,
        setting
      );
      if (addRet.status == FuncStatus.SUCCESS) {
        expenseAddedFlag = true;
      } else {
        logger.error(`error at saveExpenseFromAmazonItem:${addRet.message}`);
      }
    }

    return expenseAddedFlag
      ? {
          status: FuncStatus.SUCCESS,
          message: `at least one expense was added`,
        }
      : {
          status: FuncStatus.ERROR,
          message: `No expense was added. Unable to extract any expenses`,
        };
  }

  async saveExpenseFromUdemmy(
    rawText: string,
    setting: UdemySetting,
    categories: Record<string, Category>,
    internalDate?: string | null
  ): Promise<FuncResult> {
    if (!internalDate) {
      return {
        status: FuncStatus.ERROR,
        message: "when saving from udemy, internalDate should be given.",
      };
    }
    const parser = new UdemyMailParser(rawText, internalDate);
    const parseRet = parser.toExpenses();
    if (parseRet.status != FuncStatus.SUCCESS) {
      return parseRet;
    } else if (!parseRet.data) {
      return {
        status: FuncStatus.ERROR,
        message: "toExpenses status was Success, but data was not attached.",
      };
    } else {
      /* Do nothing */
    }

    const expensesAdded = parseRet.data;

    /* 一個でもaddできたらtrueに設定する */
    let expenseAddedFlag = false;
    for (const expense of expensesAdded) {
      const expenseWithCategory = assignCategoryById(
        expense,
        setting.categoryId,
        categories
      );

      const addRet = await this.addExpenseFromMailExtraction(
        expenseWithCategory,
        setting
      );

      if (addRet.status == FuncStatus.SUCCESS) {
        expenseAddedFlag = true;
      } else {
        logger.error(`saveExpenseFromUdemy: ${addRet.message}`);
      }
    }

    return expenseAddedFlag
      ? {
          status: FuncStatus.SUCCESS,
          message: `At least one expense was added.`,
        }
      : {
          status: FuncStatus.ERROR,
          message: `No expense was added`,
        };
  }

  async saveExpenseFromRakutenCardETC(
    rawText: string,
    setting: RakutenCardETCSetting,
    categories: Record<string, Category>
  ): Promise<FuncResult> {
    const parser = new RakutenCardETCParser(rawText);

    const ret = parser.toExpenses();
    if (ret.status != FuncStatus.SUCCESS) {
      return ret;
    } else if (!ret.data) {
      return {
        status: FuncStatus.ERROR,
        message: `data was not attached. ${ret.message}`,
      };
    } else {
      /* 特に問題ない */
    }

    const expenses = ret.data;
    let expensesAdded = false;
    for (const expense of expenses) {
      const expenseWithCategory = assignCategoryById(
        expense,
        setting.categoryId,
        categories
      );

      const addRet = await this.addExpenseFromMailExtraction(
        expenseWithCategory,
        setting
      );
      if (addRet.status == FuncStatus.SUCCESS) {
        expensesAdded = true;
      } else {
        logger.error(addRet.message);
      }
    }

    return expensesAdded
      ? {
          status: FuncStatus.SUCCESS,
          message: `More than 1 expense was added from Rakuten ETC`,
        }
      : {
          status: FuncStatus.ERROR,
          message: "No expense was added from Rakuten ETC",
        };
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
    const gmailClientRet = await generateGmailApiInstance(
      this.userId,
      this.mailboxExtractionService
    );
    if (gmailClientRet.status != FuncStatus.SUCCESS || !gmailClientRet.data) {
      logger.info(`${gmailClientRet.message}`);
      return;
    }
    const gmailClient: GmailApiClient = gmailClientRet.data;

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
      gmailClient,
      queryAfter,
      queryBefore
    );

    if (!queryRet.data || queryRet.data.length === 0) {
      /**
       * 何もヒットしなかった
       */
      logger.info("Nothing was found After query.");
      const newLastExec: LastMailboxExtractionExec = {
        ...lastExecRet.data,
        timestamp: endTime /* UNIXミリ秒で保存 */,
      };
      const ret =
        await this.mailboxExtractionService.setMailboxExtractionLastExec(
          this.userId,
          type,
          newLastExec
        );
      if (ret.status != FuncStatus.SUCCESS) {
        logger.error(`${ret.message}`);
      } else {
        logger.info(`Updated last exec. ${JSON.stringify({ newLastExec })}`);
      }
    } else {
      /**
       * クエリでなにかしらヒットした
       * まずはヒットしたすべてのIDを格納して、mapとして持っておく
       */
      const hitMsgIds = queryRet.data;
      logger.info(`Found mails ${queryRet.data.length}`);
      const sortRet = await getMessageDetailsSortedList(gmailClient, hitMsgIds);
      if (sortRet.status != FuncStatus.SUCCESS || !sortRet.data) {
        logger.error(sortRet.message);
        return;
      }

      const sortedList = sortRet.data;
      const filteredMessages: Record<string, gmail_v1.Schema$Message> = {};
      let mostRecentMsgId: string | null =
        null; /* 最後にLastExecを更新するときに使う */
      for (const [id, message] of sortedList) {
        if (id === lastMsgId) {
          logger.info(`Found lastMsgId again.${id}`);
          break; // これより古いメッセージは無視
        }

        if (!mostRecentMsgId) {
          mostRecentMsgId = id; /* 最後にいれたメッセージが一番新しい */
        }
        filteredMessages[id] = message;
      }

      /**
       * Amazon定期便の場合は"配達中:"のメールを検知している。
       * 配達された商品がAmazon定期便のものかどうかをチェックし、
       * 定期便でないものは通常購入なので、スルーする。
       * 配達中でもExpense登録してしまうとAmazonItemと二重登録になってしまう。
       * */

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
  async processAllMailTypeList(mailTypeList: AllMailType[]) {
    for (const type of mailTypeList) {
      await this.processSingleMailType(type);
    }
  }

  /**
   * Amazon定期便登録リストのモニター関数
   */
}
