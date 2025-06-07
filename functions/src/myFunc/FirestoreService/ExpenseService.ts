import { Firestore } from "firebase-admin/firestore";
import { Expense } from "../../type/Expense";
import {
  FirestoreAddResult,
  FuncResult,
  FuncStatus,
} from "../../type/FuncStatus";

export class ExpenseService {
  private db: Firestore;

  constructor(db: Firestore) {
    this.db = db;
  }

  private getUserExpensesColRef(userId: string) {
    return this.db.collection("users").doc(userId).collection("expenses");
  }

  async addExpense(
    userId: string,
    expenseData: Expense
  ): Promise<FirestoreAddResult> {
    try {
      const expensesRef = this.getUserExpensesColRef(userId);
      const docRef = await expensesRef.add(expenseData);
      return {
        status: FuncStatus.SUCCESS,
        id: docRef.id,
        message: `Expense added with ID: ${docRef.id}`,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to add expense: ${error.message}`,
      };
    }
  }

  async updateExpense(
    userId: string,
    expenseData: Expense
  ): Promise<FuncResult> {
    if (!expenseData.id)
      return {
        status: FuncStatus.ERROR,
        message: "Expense ID is required for update.",
      };
    const expenseRef = this.getUserExpensesColRef(userId).doc(expenseData.id);
    try {
      // const docSnapshot = await expenseRef.get();
      // if (!docSnapshot.exists)
      //   return {
      //     status: FuncStatus.ERROR,
      //     message: `Expense with ID ${expenseData.id} does not exist.`,
      //   };
      // await expenseRef.set(expenseData);

      /* ここはデバッグしていない!!! */
      const { id, ...updatedExpense } = expenseData;
      await expenseRef.update(updatedExpense);
      return {
        status: FuncStatus.SUCCESS,
        message: `Expense with ID ${expenseData.id} updated successfully.`,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to update expense: ${error.message}`,
      };
    }
  }

  async addExpenseWithId(
    userId: string,
    expenseData: Expense
  ): Promise<FuncResult> {
    try {
      const expensesRef = this.getUserExpensesColRef(userId);
      const newDocRef = expensesRef.doc(); // IDを事前に生成
      expenseData.id = newDocRef.id; // expense にセット

      await newDocRef.set(expenseData); // 一発で書き込み

      return {
        status: FuncStatus.SUCCESS,
        message: `Expense added with ID: ${newDocRef.id}`,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to add expense with ID: ${error.message}`,
      };
    }
  }
}
