import axios from "axios";
import { onSchedule } from "firebase-functions/scheduler";
import * as functions from "firebase-functions/v1";
import * as qs from "querystring";
import { TriggerTimeZone } from "./constants/TimeZone";
import { initializeServices } from "./myFunc/initializeServices";
import { FuncStatus } from "./type/FuncStatus";

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
 * ユーザーが作成されたときに走らせる
 * 注意：Gen1はnode18以前でないとdeployに失敗する。package.jsonの"engines"の"node"をアップデートしてはいけない！
 * 今のところ、Gen2にonUserCreateみたいな関数はないので、nodeもこのままにしておく。2025/6/4
 */
exports.onUserCreate = functions.auth.user().onCreate(async (user) => {
  const uid = user.uid;
  const email = user.email;

  console.log(`New user created! id:${uid} email:${email}`);

  if (email == undefined) {
    console.error("Unable to get Email..");
  } else {
    userSettingsProcessor.setInitialUserSettings(uid, email);
  }
});

/**
 * gmailから抽出する
 */

/**
 * グローバル変数のcachedCredentialsがnullだったら(Cold Start)読み込み
 * nullでなかったらそのまま保持している値を使う
 *  */
// async function getCredentialsFor() {
//   if (!cachedCredentials) {
//     const [version] = await secretClient.accessSecretVersion({
//       name: "projects/YOUR_PROJECT_ID/secrets/gmail-credentials/versions/latest",
//     });

//     const secretPayload = version.payload.data.toString("utf8");
//     cachedCredentials = JSON.parse(secretPayload);
//   }

//   const creds = cachedCredentials["akita"];
//   if (!creds) {
//     throw new Error(`No credentials found for ${email}`);
//   }

//   return creds;
// }

exports.handleOAuthCallback = functions
  .runWith({ secrets: ["GOOGLE_OAUTH2"] })
  .https.onRequest(async (req, res) => {
    const code = req.query.code;

    try {
      if (!process.env.GOOGLE_OAUTH2) {
        throw new Error("Unable to get environment variables\n");
      }

      const codeParam = req.query.code;
      if (typeof codeParam !== "string") {
        throw new Error("code must be a string");
      }

      const secret = JSON.parse(process.env.GOOGLE_OAUTH2);
      const postData = qs.stringify({
        code: codeParam,
        client_id: secret.client_id,
        client_secret: secret.client_secret,
        redirect_uri: secret.redirect_url, //uriが正しいらしい。でもsecretのほうにはurlで保存してしまった。
        grant_type: "authorization_code",
      });
      if (!secret.client_id || !secret.client_secret || !secret.redirect_url) {
        throw new Error("Unable to get secrets.");
      }
      const tokenRes = await axios.post(
        "https://oauth2.googleapis.com/token",
        postData,
        {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
        }
      );

      const { access_token, refresh_token } = tokenRes.data;
      if (!access_token || !refresh_token) {
        throw new Error("Unable to get access token or refresh token.");
      }

      /* refresh tokenをFirestoreに保存 */
      res.send(`I'm God Akita.\nProcess finished.\nPlease close this window.`);
    } catch (err) {
      if (axios.isAxiosError(err)) {
        console.error("Axios error:", err.response?.data);
      } else {
        console.error("Unexpected error:", err);
      }
      res.status(500).send(`OAuth token exchange failed.`);
    }
  });
