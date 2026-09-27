import { assignCategoryFromAssignmentData } from "../../domain/categoryAssign";
import { Expense } from "../../type/Expense";
import { FuncResult, FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { ExtractionContext } from "../mailSource";

export const extracted = (expenses: Expense[]): FuncResultWithData<Expense[]> => ({
  status: FuncStatus.SUCCESS,
  data: expenses,
});

/** Passes on a failed result without its data. */
export const failed = (result: FuncResult): FuncResultWithData<Expense[]> => ({
  status: result.status,
  message: result.message,
});

export const missingInternalDate = (mailName: string): FuncResultWithData<Expense[]> => ({
  status: FuncStatus.ERROR,
  message: `when saving ${mailName}, internalDate should not be empty.`,
});

export const noDataAttached = (mailName: string): FuncResultWithData<Expense[]> => ({
  status: FuncStatus.ERROR,
  message: `${mailName}: parsing was successful, but data was not attached.`,
});

/** Assigns a category with the user's rules for store names. */
export const assignByStoreName = (expense: Expense, context: ExtractionContext): Expense =>
  context.categoryAssignmentData.storeName && expense.storeName
    ? assignCategoryFromAssignmentData(
        expense,
        expense.storeName,
        context.categoryAssignmentData.storeName,
        context.categories
      )
    : expense;

/** Assigns a category with the user's rules for product names. */
export const assignByProductName = (expense: Expense, context: ExtractionContext): Expense =>
  context.categoryAssignmentData.productName && expense.itemName
    ? assignCategoryFromAssignmentData(
        expense,
        expense.itemName,
        context.categoryAssignmentData.productName,
        context.categories
      )
    : expense;
