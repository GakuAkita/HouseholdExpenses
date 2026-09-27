import { assignCategoryFromAssignmentData } from "../../domain/categoryAssign";
import { Expense } from "../../type/Expense";
import { MailParseError } from "../parsers/MailParserBase";
import { ExtractionContext } from "../mailSource";

/** Gmail's internalDate, required by mails that don't have the date in the body. */
export const requireInternalDate = (internalDate: string | null | undefined, mailName: string): string => {
  if (!internalDate) {
    throw new MailParseError(`when saving ${mailName}, internalDate should not be empty.`);
  }
  return internalDate;
};

/** Assigns a category with the user's rules for store names. */
export const assignByStoreName = (expense: Expense, context: ExtractionContext): Expense =>
  expense.storeName
    ? assignCategoryFromAssignmentData(
        expense,
        expense.storeName,
        context.categoryAssignmentData.storeName,
        context.categories
      )
    : expense;

/** Assigns a category with the user's rules for product names. */
export const assignByProductName = (expense: Expense, context: ExtractionContext): Expense =>
  expense.itemName
    ? assignCategoryFromAssignmentData(
        expense,
        expense.itemName,
        context.categoryAssignmentData.productName,
        context.categories
      )
    : expense;
