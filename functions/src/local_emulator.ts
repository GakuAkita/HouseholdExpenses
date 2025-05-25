import * as dotenv from "dotenv";
import * as path from "path";
import { ExpenseService } from "./myFunc/FirestoreService/ExpenseService";
import { FirestoreService } from "./myFunc/FirestoreService/FirestoreService";
import { RepeatAddService } from "./myFunc/FirestoreService/RepeatAddService";
import { UserService } from "./myFunc/FirestoreService/UserService";
import { RepeatAddProcessor } from "./myFunc/Processor/RepeatAddProcessor";
import { FuncStatus } from "./type/FuncStatus";

// 環境変数を読み込む
dotenv.config({ path: path.resolve(__dirname, "../../.env.local") });

const firebaseOptions = {
  projectId: process.env.FIREBASE_PROJECT_ID,
};
const fsService = new FirestoreService(firebaseOptions);
const db = fsService.getDb();

const userService = new UserService(db);
const expenseService = new ExpenseService(db);
const repeatAddService = new RepeatAddService(db);

const repeatAddProcessor = new RepeatAddProcessor(
  repeatAddService,
  expenseService
);

// const main = async () => {
//   const userId: string = "testUser";

//   await userService.addUserCol(userId);

//   const sampleCategory: Category = {
//     id: "category1",
//     timestamp: Date.now(),
//     name: "Food",
//     enabled: true,
//   };
//   const sampleExpense: Expense = {
//     timestamp: Date.now(),
//     amount: 100,
//     category: sampleCategory,
//   };
//   expenseService.addExpenseWithId(userId, sampleExpense);

//   const sampleRepeatAdd = {
//     id: "Ld0bXW2F6kfuySfG1jqy",
//     timestamp: Date.now(),
//     expense: sampleExpense,
//     frequencyInfo: {
//       frequency: "every_week",
//       dayOfWeek: ["Monday", "Wednesday"],
//       hour: 10,
//       minute: 30,
//     },
//   };
//   const ret = await repeatAddService.addRepeatAddWithId(
//     userId,
//     sampleRepeatAdd
//   );
//   if (ret.status === FuncStatus.SUCCESS) {
//     console.log("RepeatAdd updated successfully:", ret);
//   } else {
//     console.error("Failed to update RepeatAdd:", ret.message);
//   }

//   console.log("Data written to emulator.");
// };

// main();

const schedule_func = async () => {
  /* ユーザーIDをすべて取得してくる */
  let funcResult = await userService.getAllUserIds();
  if (funcResult.status !== FuncStatus.SUCCESS) {
    console.error("Failed to retrieve user IDs:", funcResult.message);
    return;
  }

  const userIds = funcResult.data;
  if (userIds == null) {
    console.error("No user IDs found.");
    return;
  }
  console.log(`Found ${userIds.length} users.`);
  for (const uid of userIds) {
    repeatAddProcessor.addExpensesFromAllRepeatAdd(uid);
    break; // 1人のユーザのみ処理
  }
};
schedule_func();
