import { onSchedule } from "firebase-functions/scheduler";
import { initializeServices } from "./myFunc/initializeServices";
import { FuncStatus } from "./type/FuncStatus";

const { userService, repeatAddProcessor } = initializeServices();

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
    timeZone: "Asia/Tokyo", // 日本時間に設定。これ変えたほうがいいな。じゃないと、日本がこの時間のときに、他の国はこの時間じゃない。
    concurrency: 1,
  },
  async (context) => {
    console.log("Starting monthly repeatAdd job...");
    await schedule_repeatAdd();
  }
);
