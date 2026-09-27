import { GmailClient } from "../infra/gmail/GmailApiClient";
import { Category } from "../type/Category";
import { CategoryAssignmentData } from "../type/CategoryAssignment";
import { Expense } from "../type/Expense";
import { FuncResultWithData } from "../type/FuncStatus";
import { AllMailType, AmazonSubscribeItem } from "../type/Mailbox";

/** One mail, as the parsers need it. */
export interface Mail {
  rawText: string;
  /** Gmail's internalDate (UNIX milliseconds as a string). Some mails have no date in the body. */
  internalDate?: string | null;
}

/** The user's data a source may need to finish its expenses. */
export interface ExtractionContext {
  categories: Record<string, Category>;
  categoryAssignmentData: CategoryAssignmentData;
  /** Enabled Amazon subscribe items. Loaded only when a source asks for them. */
  loadEnabledAmazonSubscribeItems(): Promise<FuncResultWithData<Record<string, AmazonSubscribeItem>>>;
}

/**
 * Everything specific to one kind of mail. To support a new mail, add a MailSource
 * and register it in sources/index.ts.
 */
export interface MailSource<S extends AllMailType = AllMailType> {
  readonly nodeName: S["nodeName"];

  /** Gmail ids of the candidate mails between after and before (UNIX seconds). */
  findMailIds(gmail: GmailClient, after: number, before: number): Promise<FuncResultWithData<string[]>>;

  /**
   * The expenses to save for one mail, with categories assigned.
   * generatedType and timestamp are added when saving.
   */
  toExpenses(mail: Mail, setting: S, context: ExtractionContext): Promise<FuncResultWithData<Expense[]>>;
}
