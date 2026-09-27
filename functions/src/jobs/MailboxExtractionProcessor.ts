import { logger } from "firebase-functions";
import { GeneratedType } from "../constants/GeneratedType";
import { CategoryService } from "../infra/firestore/CategoryService";
import { ExpenseService } from "../infra/firestore/ExpenseService";
import { createUserGmailClient } from "../infra/gmail/createUserGmailClient";
import { extractTextBody } from "../infra/gmail/extractHtmlBody";
import { filterMessages } from "../infra/gmail/filterMessages";
import { getMessageDetailsSortedList } from "../infra/gmail/getMessageDetailsMap";
import { CategoryAssignmentService } from "../infra/rtdb/CategoryAssignmentService";
import { MailboxExtractionService } from "../infra/rtdb/MailboxExtractionService";
import { ExtractionContext } from "../mail/mailSource";
import { mailSourceFor } from "../mail/sources";
import { messageOf } from "../shared/errors";
import { Runtime } from "../shared/runtime";
import { convertUnixMillisecToSec } from "../shared/unixTime";
import { Category } from "../type/Category";
import { CategoryAssignmentData } from "../type/CategoryAssignment";
import { AllMailType, AmazonSubscribeItem } from "../type/Mailbox";

/** How far back the first run for a mail type searches. */
const FIRST_RUN_LOOKBACK_MILLIS = 5 * 60 * 1000;

/**
 * Reads one user's mails and saves the expenses in them. 各ユーザーに対してインスタンスを生成する。
 */
export class MailboxExtractionProcessor {
  /* Loaded on first use and kept for this user's run. */
  private categories: Record<string, Category> | null = null;
  private categoryAssignmentData: CategoryAssignmentData | null = null;
  private amazonSubscribeItems: Record<string, AmazonSubscribeItem> | null = null;

  constructor(
    private userId: string,
    private mailboxExtractionService: MailboxExtractionService,
    private expenseService: Pick<ExpenseService, "addExpenseWithId">,
    private categoryService: Pick<CategoryService, "getAllCategories">,
    private categoryAssignmentService: Pick<CategoryAssignmentService, "getCategoryAssignmentData">,
    private runtime: Runtime
  ) {}

  /** すべてのメールタイプに対して実行する. A mail type that fails is logged and the next one runs. */
  async processAllMailTypeList(mailTypeList: AllMailType[]): Promise<void> {
    for (const type of mailTypeList) {
      try {
        await this.processSingleMailType(type);
      } catch (error) {
        logger.error(`${type.nodeName} failed for user ${this.userId}: ${messageOf(error)}`);
      }
    }
  }

  /**
   * Reads the mails of the type that arrived since the last run and saves their expenses.
   * @todo 基本Gmailだが将来的にOutlookとか増えた場合、ここの処理の変更が必要。
   */
  async processSingleMailType(type: AllMailType): Promise<void> {
    const setting = await this.mailboxExtractionService.getMailTypeSetting(this.userId, type);
    if (!setting) {
      /* まだユーザーが設定していないのでやらない */
      logger.debug(`Skip ${type.nodeName}. The user hasn't set it.`);
      return;
    }
    if (setting.enabled == false) {
      logger.debug(`Skip ${type.nodeName} Not Enabled.`);
      return;
    }

    const source = mailSourceFor(setting);
    if (!source) {
      throw new Error(`Not prepared type for MailboxExtraction: ${type.nodeName}`);
    }

    const lastExec = await this.mailboxExtractionService.getLastExec(this.userId, type);
    /* データベースにもミリ秒で保存する */
    const endTime = this.runtime.clock.now().getTime();
    const startTime = lastExec?.timestamp || endTime - FIRST_RUN_LOOKBACK_MILLIS;

    const gmailClient = await createUserGmailClient(this.userId, this.mailboxExtractionService, this.runtime);
    if (!gmailClient) {
      logger.info(`Gmail Token is not set by the user`);
      return;
    }

    /* emulatorの場合は1をいれて全部取ってくる */
    const queryAfter = this.runtime.config.isEmulator ? 1 : convertUnixMillisecToSec(startTime);
    const msgIds = await source.findMailIds(gmailClient, queryAfter, convertUnixMillisecToSec(endTime));

    if (msgIds.length === 0) {
      logger.info("Nothing was found After query.");
      const newLastExec = { ...lastExec, timestamp: endTime /* UNIXミリ秒で保存 */ };
      await this.mailboxExtractionService.setLastExec(this.userId, type, newLastExec);
      logger.info(`Updated last exec. ${JSON.stringify({ newLastExec })}`);
      return;
    }

    logger.info(`Found mails ${msgIds.length}`);
    const sortedList = await getMessageDetailsSortedList(gmailClient, msgIds);
    const { filteredMessages, mostRecentMsgId } = filterMessages(sortedList, lastExec?.lastMsgId);
    if (Object.keys(filteredMessages).length === 0) {
      /* Every mail found was already processed. last_exec is kept as it is. */
      logger.warn(`processSingleMailType: No new messages found. Probably this is not error.`);
      return;
    }

    /* 失敗しようが何しようが次のメールに行く */
    for (const [id, message] of Object.entries(filteredMessages)) {
      const rawText = extractTextBody(message.payload);
      if (!rawText) {
        logger.error(`Failed to extract Text Body. id=${id}`);
        continue;
      }
      try {
        /* メールによっては日時が本文内にないケースが有る */
        await this.saveExpenseWithExtraction(setting, rawText, message.internalDate);
      } catch (error) {
        logger.error(`${type.nodeName}: failed to save the mail ${id}: ${messageOf(error)}`);
      }
    }

    await this.mailboxExtractionService.setLastExec(this.userId, type, {
      timestamp: endTime,
      lastMsgId: mostRecentMsgId,
    });
  }

  /**
   * メールの本文からデータを抽出してExpenseの保存まで行う。
   * Returns the number of expenses saved. Throws when the mail can't be read.
   */
  async saveExpenseWithExtraction(
    setting: AllMailType,
    rawText: string,
    internalDate?: string | null
  ): Promise<number> {
    const source = mailSourceFor(setting);
    if (!source) {
      throw new Error(`Not prepared type for MailboxExtraction: ${setting.nodeName}`);
    }
    const expenses = await source.toExpenses(
      { rawText, internalDate },
      setting,
      await this.loadExtractionContext()
    );

    const generatedType = `${GeneratedType.MAIL_EXTRACTION}___${setting.nodeName}`;
    let added = 0;
    for (const expense of expenses) {
      try {
        await this.expenseService.addExpenseWithId(this.userId, {
          ...expense,
          generatedType,
          timestamp: this.runtime.clock.now().getTime(),
        });
        added++;
      } catch (error) {
        logger.error(`${setting.nodeName}: ${messageOf(error)}`);
      }
    }
    if (expenses.length > 0 && added === 0) {
      throw new Error(`${setting.nodeName}: No expense was added.`);
    }
    return added;
  }

  /**
   * カテゴリーとカテゴリー割当を読み込む。
   * 読み込みに失敗した場合はログ表示だけにして、空のまま続ける。
   */
  private async loadExtractionContext(): Promise<ExtractionContext> {
    if (this.categories === null) {
      try {
        this.categories = await this.categoryService.getAllCategories(this.userId);
      } catch (error) {
        logger.error(`Failed to load categories: ${messageOf(error)}`);
      }
    }
    if (this.categoryAssignmentData === null) {
      try {
        this.categoryAssignmentData = await this.categoryAssignmentService.getCategoryAssignmentData(
          this.userId
        );
      } catch (error) {
        logger.error(`Failed to load category assignment data: ${messageOf(error)}`);
      }
    }
    return {
      categories: this.categories ?? {},
      categoryAssignmentData: this.categoryAssignmentData ?? { storeName: {}, productName: {} },
      loadEnabledAmazonSubscribeItems: () => this.loadEnabledAmazonSubscribeItems(),
    };
  }

  /** enabled=falseのAmazon定期便アイテムを除外する（enabledがnullの場合はtrueとして扱う） */
  private async loadEnabledAmazonSubscribeItems(): Promise<Record<string, AmazonSubscribeItem>> {
    this.amazonSubscribeItems ??= await this.mailboxExtractionService.getAmazonSubscribeItems(this.userId);
    return Object.fromEntries(
      Object.entries(this.amazonSubscribeItems).filter(([, item]) => item.enabled !== false)
    );
  }
}
