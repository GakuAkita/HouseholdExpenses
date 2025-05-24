// firestoreService.ts
import * as admin from "firebase-admin";
import { Firestore } from "firebase-admin/firestore";
import { initMyFirebaseAdmin } from "../myFunc/firebaseAdmin";
import { Expense } from "../type/Expense";
import { FirestoreAddResult, FuncResult, FuncStatus } from "../type/FuncStatus";
import { RepeatAdd } from "../type/RepeatAdd";

export class FirestoreService {
  private db: Firestore;

  constructor(options?: admin.AppOptions) {
    initMyFirebaseAdmin(options); // オプションがあればローカル
    this.db = require("firebase-admin").firestore();
  }

  /* Reference関連 */
  get usersColRef() {
    return this.db.collection("users");
  }

  getUserDocRef(userId: string) {
    return this.usersColRef.doc(userId);
  }

  getUserExpensesColRef(userId: string) {
    return this.getUserDocRef(userId).collection("expenses");
  }

  getUserRepeatAddColRef(userId: string) {
    return this.getUserDocRef(userId).collection("repeatAdd");
  }

  getUserCategoriesColRef(userId: string) {
    return this.getUserDocRef(userId).collection("categories");
  }

  /* Firestoreを実際に操作する */
  async updateExpense(
    userId: string,
    expenseData: Expense
  ): Promise<FuncResult> {
    if (!expenseData.id) {
      const ret: FuncResult = {
        status: FuncStatus.ERROR,
        message: "Expense ID is required for update.",
      };
      return ret;
    }

    const expenseId = expenseData.id;
    const expenseRef = this.getUserExpensesColRef(userId).doc(expenseId);

    try {
      const docSnapshot = await expenseRef.get();

      if (!docSnapshot.exists) {
        return {
          status: FuncStatus.ERROR,
          message: `Expense with ID ${expenseId} does not exist.`,
        };
      }

      await expenseRef.set(expenseData);

      const ret: FuncResult = {
        status: FuncStatus.SUCCESS,
        message: `Expense with ID ${expenseId} updated successfully.`,
      };
      return ret;
    } catch (error: any) {
      const ret: FuncResult = {
        status: FuncStatus.ERROR,
        message: `Failed to update expense: ${error.message}`,
      };
      return ret;
    }
  }

  async addExpense(
    userId: string,
    expenseData: Expense
  ): Promise<FirestoreAddResult> {
    try {
      const expensesRef = this.getUserExpensesColRef(userId);
      const docRef = await expensesRef.add(expenseData);
      const ret: FirestoreAddResult = {
        status: FuncStatus.SUCCESS,
        id: docRef.id,
        message: `Expense added with ID: ${docRef.id}`,
      };
      return ret;
    } catch (error: any) {
      const ret: FirestoreAddResult = {
        status: FuncStatus.ERROR,
        message: `Failed to add expense: ${error.message}`,
      };
      return ret;
    }
  }

  async addExpenseWithId(
    userId: string,
    expenseData: Expense
  ): Promise<FuncResult> {
    const addStatus: FirestoreAddResult = await this.addExpense(
      userId,
      expenseData
    );

    if (addStatus.status != FuncStatus.SUCCESS) {
      return addStatus;
    }

    if (!addStatus.id) {
      return {
        status: FuncStatus.ERROR,
        message: "Failed to retrieve the added expense ID.",
      };
    }

    // 成功した場合は、IDをexpenseDataに設定して返す
    expenseData.id = addStatus.id;
    console.log("Expense added with ID:", expenseData.id);
    console.log("Expense data:", expenseData);

    //最後にupdateする
    const updateStatus = await this.updateExpense(userId, expenseData);
    return updateStatus;
  }

  /* RepeatAddを追加する */
  async addRepeatAdd(
    userId: string,
    repeatAddData: RepeatAdd
  ): Promise<FirestoreAddResult> {
    try {
      const repeatAddRef = this.getUserRepeatAddColRef(userId);
      const docRef = await repeatAddRef.add(repeatAddData);
      return {
        status: FuncStatus.SUCCESS,
        id: docRef.id,
        message: `RepeatAdd added with ID: ${docRef.id}`,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to add RepeatAdd: ${error.message}`,
      };
    }
  }

  /* RepeatAddを更新する */
  async updateRepeatAdd(
    userId: string,
    repeatAddData: RepeatAdd
  ): Promise<FuncResult> {
    if (!repeatAddData.id) {
      return {
        status: FuncStatus.ERROR,
        message: "RepeatAdd ID is required for update.",
      };
    }

    const repeatAddId = repeatAddData.id;
    const repeatAddRef = this.getUserRepeatAddColRef(userId).doc(repeatAddId);

    try {
      const docSnapshot = await repeatAddRef.get();

      if (!docSnapshot.exists) {
        return {
          status: FuncStatus.ERROR,
          message: `RepeatAdd with ID ${repeatAddId} does not exist.`,
        };
      }

      await repeatAddRef.set(repeatAddData);

      return {
        status: FuncStatus.SUCCESS,
        message: `RepeatAdd with ID ${repeatAddId} updated successfully.`,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to update RepeatAdd: ${error.message}`,
      };
    }
  }

  /* RepeatAddを追加してIDをセットし、最後に更新 */
  async addRepeatAddWithId(
    userId: string,
    repeatAddData: RepeatAdd
  ): Promise<FuncResult> {
    const addStatus = await this.addRepeatAdd(userId, repeatAddData);

    if (addStatus.status !== FuncStatus.SUCCESS) {
      return addStatus;
    }

    if (!addStatus.id) {
      return {
        status: FuncStatus.ERROR,
        message: "Failed to retrieve the added RepeatAdd ID.",
      };
    }

    repeatAddData.id = addStatus.id;
    console.log("RepeatAdd added with ID:", repeatAddData.id);
    console.log("RepeatAdd data:", repeatAddData);

    const updateStatus = await this.updateRepeatAdd(userId, repeatAddData);
    return updateStatus;
  }
}
