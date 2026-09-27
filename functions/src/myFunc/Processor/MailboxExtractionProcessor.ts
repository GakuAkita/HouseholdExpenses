import { logger } from "firebase-functions";
import { GeneratedType } from "../../constants/GeneratedType";
import { ExtractionContext } from "../../mail/mailSource";
import { mailSourceFor } from "../../mail/sources";
import { Runtime } from "../../shared/runtime";
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
  AmazonSubscribeItem,
  LastMailboxExtractionExec,
} from "../../type/Mailbox";
import { GmailClient } from "../Client/GmailApiClient";
import { CategoryService } from "../FirestoreService/CategoryService";
import { ExpenseService } from "../FirestoreService/ExpenseService";
import { CategoryAssignmentService } from "../RealtimeDbService/CategoryAssignmentService";
import { MailboxExtractionService } from "../RealtimeDbService/MailboxExtractionService";
import { convertUnixMillisecToSec } from "../utility/getCurrentUnixSec";
import { extractTextBody } from "../utility/gmail/extractHtmlBody";
import { filterMessages } from "../utility/gmail/filterMessages";
import { generateGmailApiInstance } from "../utility/gmail/generateGmailApiInstance";
import { getMessageDetailsSortedList } from "../utility/gmail/getMessageDetailsMap";

/**
 * 各ユーザーに対してインスタンスを生成することにする！
 */
export class MailboxExtractionProcessor {
  private userId: string;
  private categories: Record<string, Category> | null = null;
  private categoryAssignmentData: CategoryAssignmentData | null =
    null; /* 今のところ毎回全部取るが、将来的に商品名か店名の片方で良いかも */
  private amazonSubscribeItems: Record<string, AmazonSubscribeItem> | null = null;

  constructor(
    userId: string,
    private mailboxExtractionService: MailboxExtractionService,
    private expenseService: ExpenseService,
    private categoryService: CategoryService,
    private categoryAssignmentService: CategoryAssignmentService,
    private runtime: Runtime
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

  /* ************************Amazon定期便アイテムの読み込み***************************** */
  private async loadAmazonSubscribeItems(): Promise<
    FuncResultWithData<Record<string, AmazonSubscribeItem>>
  > {
    if (this.amazonSubscribeItems === null) {
      const result = await this.mailboxExtractionService.getAmazonSubscribeMonitorItems(this.userId);
      if (result.status !== FuncStatus.SUCCESS) {
        return result;
      }

      if (!result.data) {
        /* 定期便アイテムがない可能性もあるから、成功として扱う。 */
        this.amazonSubscribeItems = {};
        return {
          status: FuncStatus.SUCCESS,
          message: "There was no error in getAmazonSubscribeMonitorItems, but empty.",
          data: this.amazonSubscribeItems,
        };
      }
      this.amazonSubscribeItems = result.data;
    }

    return {
      status: FuncStatus.SUCCESS,
      message: "Already loaded before.",
      data: this.amazonSubscribeItems,
    };
  }

  /**
   * enabled=trueまたはnullのAmazon定期便アイテムのみを取得する
   * enabledがnullの場合はtrueとして扱う
   */
  private async loadEnabledAmazonSubscribeItems(): Promise<
    FuncResultWithData<Record<string, AmazonSubscribeItem>>
  > {
    const result = await this.loadAmazonSubscribeItems();
    if (result.status !== FuncStatus.SUCCESS || !result.data) {
      return result;
    }

    // enabled=falseのアイテムを除外（enabledがnullの場合はtrueとして扱う）
    const filteredItems: Record<string, AmazonSubscribeItem> = {};
    for (const [id, item] of Object.entries(result.data)) {
      if (item.enabled !== false) {
        filteredItems[id] = item;
      }
    }

    return {
      status: FuncStatus.SUCCESS,
      message: "Filtered enabled Amazon Subscribe items",
      data: filteredItems,
    };
  }

  /* *****************************Gmailのクエリ関係************************************ */
  async getMailIdsByQuery(
    type: AllMailType,
    gmailClient: GmailClient,
    startTime: number,
    endTime: number
  ): Promise<FuncResultWithData<string[]>> {
    const source = mailSourceFor(type);
    if (!source) {
      return {
        status: FuncStatus.ERROR,
        message: `Unknown type:${type.nodeName}`,
      };
    }
    return source.findMailIds(gmailClient, startTime, endTime);
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
    const timestamp = this.runtime.clock.now().getTime();

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
    const source = mailSourceFor(setting);
    if (!source) {
      logger.error(`Not prepared type for MailboxExtraction: ${nodeName}`);
      return {
        status: FuncStatus.ERROR,
        message: `Not prepared type for MailboxExtraction: ${nodeName}`,
      };
    }

    const context = await this.loadExtractionContext();
    const ret = await source.toExpenses(
      { rawText, internalDate: sentDate },
      setting,
      context
    );
    if (ret.status != FuncStatus.SUCCESS || !ret.data) {
      return { status: ret.status, message: ret.message };
    }
    if (ret.data.length === 0) {
      /* e.g. Amazon定期便: no dispatched product is in the subscribe list */
      return {
        status: FuncStatus.SUCCESS,
        message: `${nodeName}: No expense to save from this mail.`,
      };
    }

    /* 一個でもaddできたら成功とする */
    let addedCount = 0;
    for (const expense of ret.data) {
      const addRet = await this.addExpenseFromMailExtraction(expense, setting);
      if (addRet.status == FuncStatus.SUCCESS) {
        addedCount++;
      } else {
        logger.error(`${nodeName}: ${addRet.message}`);
      }
    }

    return addedCount > 0
      ? {
        status: FuncStatus.SUCCESS,
        message: `${nodeName}: ${addedCount} of ${ret.data.length} expenses were added.`,
      }
      : {
        status: FuncStatus.ERROR,
        message: `${nodeName}: No expense was added.`,
      };
  }

  /**
   * カテゴリーとカテゴリー割当を読み込む。
   * 読み込みに失敗した場合はログ表示だけにして、空のまま続ける。
   */
  private async loadExtractionContext(): Promise<ExtractionContext> {
    let categories: Record<string, Category> = {};
    const categoryRet = await this.loadCategories();
    if (categoryRet.status != FuncStatus.SUCCESS) {
      logger.error(`Failed to load categories: ${categoryRet.message}`);
    } else if (categoryRet.data) {
      categories = categoryRet.data;
    }

    let categoryAssignmentData: CategoryAssignmentData = {
      storeName: {},
      productName: {},
    };
    const assignRet = await this.loadCategoryAssignmentData();
    if (assignRet.status != FuncStatus.SUCCESS) {
      logger.error(
        `Failed to load category assignment data: ${assignRet.message}`
      );
    } else if (assignRet.data) {
      categoryAssignmentData = assignRet.data;
    }

    return {
      categories,
      categoryAssignmentData,
      loadEnabledAmazonSubscribeItems: () =>
        this.loadEnabledAmazonSubscribeItems(),
    };
  }

  /* ******************************実際に呼び出す処理(全体)************************************* */
  /**
   * @todo
   * 基本Gmailだが将来的にOutlookとか増えた場合、ここの処理の変更が必要。
   * 一応、各メールテンプレートのdata classになんのメールで登録しているか持たせている。(今は全部Gmailだが)
   */
  async processSingleMailType(type: AllMailType) {
    const funcName = "processSingleMailType";
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
    const endTime = this.runtime.clock.now().getTime();
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
      this.mailboxExtractionService,
      this.runtime
    );
    if (gmailClientRet.status != FuncStatus.SUCCESS || !gmailClientRet.data) {
      logger.info(`${gmailClientRet.message}`);
      return;
    }
    const gmailClient: GmailClient = gmailClientRet.data;

    /**
     * クエリをして、msgIdを取得
     */
    const isEmulator = this.runtime.config.isEmulator;
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
        logger.error(`${sortRet.message}`);
        return;
      }

      const sortedList = sortRet.data;
      const filterRet = filterMessages(sortedList, lastMsgId);
      if (filterRet.status != FuncStatus.SUCCESS) {
        if (filterRet.status == FuncStatus.EMPTY) {
          logger.warn(`${funcName}: Probably this is not error.`);
        }
        logger.error(`${funcName}:${filterRet.message}`);
        return;
      }

      const filteredMessages = filterRet.data?.filteredMessages;
      const mostRecentMsgId = filterRet.data?.mostRecentMsgId;
      if (!filteredMessages) {
        logger.warn(`${funcName}:filteredMessages is null...`);
        return;
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

      return;
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
