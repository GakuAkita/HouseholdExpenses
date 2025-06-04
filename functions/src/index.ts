import * as admin from "firebase-admin";
import { onSchedule } from "firebase-functions/scheduler";
import { onRequest } from "firebase-functions/v2/https";
import { TriggerTimeZone } from "./constants/TimeZone";
import { initializeServices } from "./myFunc/initializeServices";
import { FuncStatus } from "./type/FuncStatus";

admin.initializeApp();

const { userService, repeatAddProcessor, userSettingsProcessor } =
  initializeServices();

const schedule_repeatAdd = async () => {
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
  }
  return;
};

exports.monthly_repeatAddJob = onSchedule(
  {
    schedule: "0 1 1 * *", // 毎月1日 1:00 JST
    timeZone: TriggerTimeZone, // 現在時刻の設定も日本にしているから、大丈夫。
    concurrency: 1,
  },
  async (_) => {
    console.log("Starting monthly repeatAdd job...");
    await schedule_repeatAdd();
  }
);

/**
 * リクエストが来たときに走らせる。
 * 認証済みの場合のみ受け取る
 */
exports.helloWorld = onRequest((request, response) => {
  response.send("Hello from Firebase!");
});
