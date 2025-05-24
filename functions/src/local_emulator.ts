import * as dotenv from "dotenv";
import * as path from "path";
import { FirestoreService } from "./FirestoreService/FirestoreService";
import { Category } from "./type/Category";
import { Expense } from "./type/Expense";

// 環境変数を読み込む
dotenv.config({ path: path.resolve(__dirname, "../../.env.local") });

const service = new FirestoreService({
  projectId: process.env.FIREBASE_PROJECT_ID, // 環境変数からprojectIdを取得
});

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
  service.addExpenseWithId(userId, sampleExpense);

  const sampleRepeatAdd = {
    id: "repeatAdd1",
    timestamp: Date.now(),
    expense: sampleExpense,
    frequencyInfo: {
      frequency: "weekly",
      dayOfWeek: ["Monday", "Wednesday"],
      hour: 10,
      minute: 30,
    },
  };

  console.log("Data written to emulator.");
};

main();
