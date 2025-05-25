import * as dotenv from "dotenv";
import * as path from "path";
import { ExpenseService } from "./myFunc/FirestoreService/ExpenseService";
import { FirestoreService } from "./myFunc/FirestoreService/FirestoreService";
import { RepeatAddService } from "./myFunc/FirestoreService/RepeatAddService";
import { Category } from "./type/Category";
import { Expense } from "./type/Expense";
import { FuncStatus } from "./type/FuncStatus";

// 環境変数を読み込む
dotenv.config({ path: path.resolve(__dirname, "../../.env.local") });

const fsService = new FirestoreService();
const db = fsService.getDb();

const expenseService = new ExpenseService(db);
const repeatAddService = new RepeatAddService(db);

const main = async () => {
  const userId: string = "testUser";

  const sampleCategory: Category = {
    id: "category1",
    timestamp: Date.now(),
    name: "Food",
    enabled: true,
  };
  const sampleExpense: Expense = {
    timestamp: Date.now(),
    amount: 100,
    category: sampleCategory,
  };
  expenseService.addExpenseWithId(userId, sampleExpense);

  const sampleRepeatAdd = {
    id: "Ld0bXW2F6kfuySfG1jqy",
    timestamp: Date.now(),
    expense: sampleExpense,
    frequencyInfo: {
      frequency: "every_week",
      dayOfWeek: ["Monday", "Wednesday"],
      hour: 10,
      minute: 30,
    },
  };
  const ret = await repeatAddService.updateRepeatAdd(userId, sampleRepeatAdd);
  if (ret.status === FuncStatus.SUCCESS) {
    console.log("RepeatAdd updated successfully:", ret);
  } else {
    console.error("Failed to update RepeatAdd:", ret.message);
  }

  console.log("Data written to emulator.");
};

// main();

const schedule_func = async () => {};
