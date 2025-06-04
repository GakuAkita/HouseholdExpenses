import * as dotenv from "dotenv";
import functions from "firebase-functions/v1"; /* これならいけるのか？？ */
import * as path from "path";
import { RepeatFrequency } from "./constants/RepeatFrequency";
import { TimeZone } from "./constants/TimeZone";
import { ExpenseService } from "./myFunc/FirestoreService/ExpenseService";
import { FirestoreService } from "./myFunc/FirestoreService/FirestoreService";
import { RepeatAddService } from "./myFunc/FirestoreService/RepeatAddService";
import { SettingsService } from "./myFunc/FirestoreService/SettingsService";
import { UserService } from "./myFunc/FirestoreService/UserService";
import { RepeatAddProcessor } from "./myFunc/Processor/RepeatAddProcessor";
import { UserSettingsProcessor } from "./myFunc/Processor/UserSettingsProcessor";
import { Category } from "./type/Category";
import { Expense } from "./type/Expense";
import { FuncStatus } from "./type/FuncStatus";
import { UserPreferences } from "./type/UserPreferences";

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
const settingsService = new SettingsService(db);

const repeatAddProcessor = new RepeatAddProcessor(
  repeatAddService,
  expenseService,
  settingsService
);

const userSettingsProcessor = new UserSettingsProcessor(
  userService,
  settingsService
);

const init_add = async () => {
  const userId: string = "testUser";

  await userService.addUserCol(userId);

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
  // expenseService.addExpenseWithId(userId, sampleExpense);

  const sampleRepeatAdd = {
    id: "Ld0bXW2F6kfuySfG1jqy",
    timestamp: Date.now(),
    expense: sampleExpense,
    frequencyInfo: {
      frequency: RepeatFrequency.EVERY_YEAR, //everydayはOK、every_weekはOK、weekendsはOK、weekdaysはOK、every_monthはOK
      month: 6,
      day: 2,
      hour: 9,
      minute: 30,
    },
  };

  const sampleUserPreferences: UserPreferences = {
    timeZone: TimeZone.JST,
  };
  const ret = await repeatAddService.addRepeatAddWithId(
    userId,
    sampleRepeatAdd
  );
  if (ret.status === FuncStatus.SUCCESS) {
    console.log("addRepeatAddWithId success", ret);
  } else {
    console.error("Failed to update RepeatAdd:", ret.message);
  }

  const set_ret = await settingsService.addUserPreferences(
    userId,
    sampleUserPreferences
  );

  if (set_ret.status === FuncStatus.SUCCESS) {
    console.log("addRepeatAddWithId success", set_ret);
  } else {
    console.error("Failed to update RepeatAdd:", set_ret.message);
  }

  console.log("Data written to emulator.");
};

// init_add();

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
    const ret = await repeatAddProcessor.addExpensesFromAllRepeatAdd(uid);
    console.log(`addExpensesFromAllRepeatAdd ${ret.message}`);
  }
};
schedule_func();

const onUserCreate = () => {
  const sampleUid = "akita_gaku";
  userSettingsProcessor.setInitialUserSettings(sampleUid, "g@gmail.com");
};

// ユーザー作成時のトリガー
exports.onUserCreate = functions.auth.user().onCreate(async (user) => {
  console.log("User created:", user.uid);
  if (!user.email) {
    console.error("user email is undefined.");
    return;
  }
  await userSettingsProcessor.setInitialUserSettings(user.uid, user.email!!);
});
