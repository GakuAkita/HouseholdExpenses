import { SecretManagerServiceClient } from "@google-cloud/secret-manager";
import axios from "axios";
import { logger } from "firebase-functions";
import { onSchedule } from "firebase-functions/scheduler";
import * as functions from "firebase-functions/v1";
import * as qs from "querystring";
import { TriggerTimeZone } from "./constants/TimeZone";
import { admin } from "./myFunc/firebaseAdmin";
import { loadGoogleOAuthSecrets } from "./myFunc/googleOAuthSecrets";
import { initializeServices } from "./myFunc/initializeServices";
import { FuncStatus } from "./type/FuncStatus";
import { GoogleOAuthSecrets } from "./type/GoogleOAuthSecrets";
import { MailboxTokenType } from "./type/Mailbox";
const {
  userService,
  repeatAddProcessor,
  userSettingsProcessor,
  mailboxExtractionService,
} = initializeServices();

/* SecretManagerを使うためにインスタンス化 */
const secretClient = new SecretManagerServiceClient();

const schedule_repeatAdd = async () => {
  /* ユーザーIDをすべて取得してくる */
  let funcResult = await userService.getAllUserIds();
  if (funcResult.status !== FuncStatus.SUCCESS) {
    logger.error("Failed to retrieve user IDs:", funcResult.message);
    return;
  }

  const userIds = funcResult.data;
  if (userIds == null) {
    logger.error("No user IDs found.");
    return;
  }
  logger.log(`Found ${userIds.length} users.`);
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
    logger.log("Starting monthly repeatAdd job...");
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

  logger.log(`New user created! id:${uid} email:${email}`);

  if (email == undefined) {
    logger.error("Unable to get Email..");
  } else {
    userSettingsProcessor.setInitialUserSettings(uid, email);
  }
});

/**
 * Gmailアクセスの許可を取得したときの処理
 */
exports.handleOAuthCallback = functions.https.onRequest(async (req, res) => {
  logger.log("Received OAuth callback request.");
  const state = req.query.state as string | undefined;
  if (!state) {
    logger.error("State parameter is missing in the request.");
    return;
  }

  const codeParam = req.query.code;
  if (typeof codeParam !== "string") {
    logger.error("Code parameter is missing or invalid in the request.");
    return;
  }

  /**
   * 暗号化用のキーを取得
   */

  try {
    /**
     * stateにFirebaseのIDトークンが入っている。
     * * これをデコードして、uidを取得する。
     *  */
    const decodedToken = await admin.auth().verifyIdToken(state);
    if (!decodedToken || !decodedToken.uid) {
      throw new Error(
        "Invalid state parameter: Unable to decode Firebase ID token."
      );
    }
    const uid = decodedToken.uid;

    let ret = await loadGoogleOAuthSecrets();
    if (ret.status !== FuncStatus.SUCCESS) {
      throw new Error(`Failed to load Google OAuth secrets: ${ret.message}`);
    }
    const secrets =
      ret.data as GoogleOAuthSecrets; /* loadGoogleOAuthSecrets内で値が入っているかチェックはしている */
    const postData = qs.stringify({
      code: codeParam,
      client_id: secrets.clientId,
      client_secret: secrets.clientSecret,
      redirect_uri: secrets.redirectUri, //uriが正しいらしい。でもsecretのほうにはurlで保存してしまった。
      grant_type: "authorization_code",
    });

    /**
     * アクセストークンとリフレッシュトークンを取得
     */
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

    /**
     * refresh_tokenを暗号化して保存する
     */

    /* ここでMailboxTokenTypeに変換しないとだめ */
    const mailboxToken: MailboxTokenType = {
      refreshToken: refresh_token,
    };

    /* ここまでちゃんとできている */
    ret =
      await mailboxExtractionService.setMailboxExtractionTokenWithEncryption(
        uid,
        mailboxToken,
        secrets.encryptionKey
      );

    if (ret.status !== FuncStatus.SUCCESS) {
      throw new Error(
        `Failed to set mailbox extraction token for user ${uid}: ${ret.message}`
      );
    }
    res.send(
      `<h1>I'm God Akita.</h1><h2>Process finished.<br>Please close this window.</h2>`
    );
  } catch (err) {
    if (axios.isAxiosError(err)) {
      logger.error("Axios error:", err.response?.data);
    } else {
      logger.error("Unexpected error:", err);
    }
    res.status(200).send(`OAuth token exchange failed.`);
  }
});
