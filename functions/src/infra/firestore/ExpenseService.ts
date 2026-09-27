import { Firestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { Expense } from "../../type/Expense";

/* The app reads a missing category as null. */
function normalizeExpenseCategory(expense: Expense): Expense {
  if (expense.category === undefined) {
    return { ...expense, category: null };
  }
  return expense;
}

export class ExpenseService {
  constructor(private db: Firestore) {}

  private getUserExpensesColRef(userId: string) {
    return this.db.collection("users").doc(userId).collection("expenses");
  }

  /**
   * Adds the expense as a new document. The document id is also written to the id field,
   * which the app requires. Returns the id.
   */
  async addExpenseWithId(userId: string, expense: Expense): Promise<string> {
    const newDocRef = this.getUserExpensesColRef(userId).doc(); // IDを事前に生成
    const data = normalizeExpenseCategory({ ...expense, id: newDocRef.id });
    await newDocRef.set(data); // 一発で書き込み
    /* ここで記録しておくことで後で戻れるようにする */
    logger.info(`Added Expense:${JSON.stringify(data)}`);
    return newDocRef.id;
  }
}
