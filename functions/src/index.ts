import { SecretManagerServiceClient } from "@google-cloud/secret-manager";
import axios from "axios";
import { logger } from "firebase-functions";
import { onSchedule } from "firebase-functions/scheduler";
import * as functions from "firebase-functions/v1";
import * as qs from "querystring";
import { TriggerTimeZone } from "./constants/TimeZone";
import { admin } from "./myFunc/firebaseAdmin";
import { initializeServices } from "./myFunc/initializeServices";
import { FuncResultWithData, FuncStatus } from "./type/FuncStatus";
import { MailboxTokenType } from "./type/Mailbox";

const {
  userService,
  repeatAddProcessor,
  userSettingsProcessor,
  mailboxExtractionService,
} = initializeServices();

/* SecretManagerを使うためにインスタンス化 */
const secretClient = new SecretManagerServiceClient();

let cachedEncryptionKey: string | null = null;
/**
 * キャッシュが残っていたらそれを返す
 * 頻繁なアクセスを避けるため
 */
const loadEncryptionKey = async (): Promise<FuncResultWithData<string>> => {
  if (cachedEncryptionKey) {
    return {
      status: FuncStatus.SUCCESS,
      message: "Encryption key loaded from cache.",
      data: cachedEncryptionKey,
    };
  }

  // ここでSecret Managerなどからキーを取得する処理を書く（例）
  const secretName = "ENCRYPTION_KEY";
  const [version] = await secretClient.accessSecretVersion({
    name: `projects/${process.env.GCLOUD_PROJECT}/secrets/${secretName}/versions/latest`,
  });
  const data = version.payload?.data as Buffer | undefined;
  if (!data) {
    return {
      status: FuncStatus.ERROR,
      message: "Failed to load encryption key from Secret Manager",
      data: undefined,
    };
  }
  const encryptionKey = Buffer.from(data).toString("utf8");
  if (!encryptionKey) {
    return {
      status: FuncStatus.ERROR,
      message: "Encryption key is empty.",
      data: undefined,
    };
  }

  /**
   * cachedEncryptionKeyはグローバル!!!
   */
  cachedEncryptionKey = encryptionKey; // キャッシュに保存
  return {
    status: FuncStatus.SUCCESS,
    message: "Encryption key loaded successfully.",
    data: cachedEncryptionKey /* 一応こっちでも返しておく */,
  };
};

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
  .runWith({
    secrets: ["GOOGLE_OAUTH2"],
  }) /* refresh tokenは何回も保存するわけではないから、runWithでいい。 */
  .https.onRequest(async (req, res) => {
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

      /**
       * 環境変数(Secret Manager)からGoogle OAuth2のクライアントID、クライアントシークレット、リダイレクトURIを取得
       */
      if (!process.env.GOOGLE_OAUTH2) {
        throw new Error("Unable to get environment variables\n");
      }
      const secret = JSON.parse(process.env.GOOGLE_OAUTH2);
      const postData = qs.stringify({
        code: codeParam,
        client_id: secret.client_id,
        client_secret: secret.client_secret,
        redirect_uri: secret.redirect_uri, //uriが正しいらしい。でもsecretのほうにはurlで保存してしまった。
        grant_type: "authorization_code",
      });
      if (!secret.client_id || !secret.client_secret || !secret.redirect_uri) {
        logger.error(
          `varibles check: client_id:${secret.client_id}, client_secret:${secret.client_secret}, redirect_uri:${secret.redirect_uri}`
        );
        throw new Error("Unable to get secrets.");
      }

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
      let ret = await loadEncryptionKey();
      if (ret.status !== FuncStatus.SUCCESS) {
        throw new Error(`Failed to load encryption key: ${ret.message}`);
      }
      const encryptionKey = ret.data;
      if (!encryptionKey) {
        throw new Error(`Encryption key is empty.${ret.message}`);
      }

      /* ここでMailboxTokenTypeに変換しないとだめ */
      const mailboxToken: MailboxTokenType = {
        refreshToken: refresh_token,
      };

      /* ここまでちゃんとできている */
      ret =
        await mailboxExtractionService.setMailboxExtractionTokenWithEncryption(
          uid,
          mailboxToken,
          encryptionKey /* 環境変数から取得した暗号化キー */
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
