import * as admin from "firebase-admin";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { getUserRepeatAddColRef } from "../../FirestoreService/FirestoreService";
import { RepeatAdd } from "../../type/RepeatAdd";
import { getMyFirestore } from "../firebaseAdmin";
import { getAllUserId } from "../utility/getUsers";

export const repeatAddFunc = onSchedule("0 0 1 * *", async (event) => {
  console.log("月初めの繰り返し追加をすべて実行します。");

  const snapshot = await admin.firestore().collectionGroup("repeatAdd").get();

  snapshot.forEach(async (doc) => {
    const data = doc.data();
    const path = doc.ref.path;
    const userId = path.split("/")[1]; //ここのパスがユーザーIDが入っている。間違っていると他のユーザーのところに書き込んでしまう。

    console.log(
      `User:${userId} Expense:${data.expenseId}のデータを追加します。`
    );

    // 1ヶ月分のデータを追加
    // for (let i = 0; i < repeatAddData.length; i++) {
    //   const addData = repeatAddData[i];
    //   const newDocRef = admin
    //     .firestore()
    //     .collection("users")
    //     .doc(userId)
    //     .collection("expenses")
    //     .doc(expenseId)
    //     .collection("repeatAdd")
    //     .doc();

    //   await newDocRef.set(addData);
    //   console.log("Added document with ID: ", newDocRef.id);
    // }
  });

  console.log("✅ 月初タスク完了");
});

export const getAllRepeatData = async () => {
  const userIds = await getAllUserId();

  const allData: Record<string, Record<string, RepeatAdd>> = {};

  for (const userId of userIds) {
    const snapshot = await getUserRepeatAddColRef(userId).get();
    const userRepeatAddMap: Record<string, RepeatAdd> = {};

    snapshot.forEach((doc) => {
      const data = doc.data() as RepeatAdd;
      userRepeatAddMap[doc.id] = data;
    });

    allData[userId] = userRepeatAddMap;
    console.log(
      `${userId}: ${
        Object.keys(userRepeatAddMap).length
      }件のデータを取得しました。`
    );
  }

  return allData;
};

export const getUserAllRepeatAddData = async (userId: String) => {
  const snapshot = await getMyFirestore().collectionGroup("repeatAdd").get();
  const allData: RepeatAdd[] = [];

  snapshot.forEach((doc) => {
    const data = doc.data() as RepeatAdd;
    allData.push(data);
  });

  return allData;
};
