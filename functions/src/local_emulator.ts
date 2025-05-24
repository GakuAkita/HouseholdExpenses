import * as dotenv from "dotenv";
import * as path from "path";
import { getMyFirestore } from "./myFunc/firebaseAdmin";

// 環境変数を読み込む
dotenv.config({ path: path.resolve(__dirname, "../../.env.local") });

const main = async () => {
  console.log("Firestore emulator ready");

  const db = getMyFirestore({
    projectId: process.env.FIREBASE_PROJECT_ID, // 環境変数からprojectIdを取得
  });
  console.log("Firestore instance created:", db);

  // 例: データ追加
  await db.collection("test").add({
    message: "Hello from emulator",
    timestamp: new Date(),
  });

  console.log("Data written to emulator.");
};

main();
