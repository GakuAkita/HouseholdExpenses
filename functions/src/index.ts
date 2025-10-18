import axios from "axios";
import { logger } from "firebase-functions";
import { onSchedule } from "firebase-functions/scheduler";
import * as functions from "firebase-functions/v1";
import * as qs from "querystring";
import { TriggerTimeZone } from "./constants/TimeZone";
import { admin } from "./myFunc/firebaseAdmin";
import { loadGoogleOAuthSecrets } from "./myFunc/googleOAuthSecrets";
import { initializeServices } from "./myFunc/initializeServices";
import { MailboxExtractionProcessor } from "./myFunc/Processor/MailboxExtractionProcessor";
import { FuncStatus } from "./type/FuncStatus";
import { GoogleOAuthSecrets } from "./type/GoogleOAuthSecrets";
import {
  AllMailType,
  mailboxExtractionSchedules,
  MailboxGmailTokenType,
} from "./type/Mailbox";
import { AmazonSubscribeMonitorItemsProcessor } from "./myFunc/Processor/AmazonSubscribeMonitorItemsProcessor";
const {
  userService,
  repeatAddProcessor,
  expenseService,
  categoryService,
  userSettingsProcessor,
  mailboxExtractionService,
  categoryAssignmentService,
} = initializeServices();

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
    /* awaitつけないとスルーされる？ */
    const addResult = await repeatAddProcessor.addExpensesFromAllRepeatAdd(uid);
    if (addResult.status !== FuncStatus.SUCCESS) {
      logger.error(
        `Failed to add expenses from repeat adds for user ${uid}: ${addResult.message}`
      );
    } else {
      logger.log(
        `Successfully added expenses from repeat adds for user ${uid}.`
      );
    }
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

exports.repeatAddTest = functions.https.onRequest(async (req, res) => {
  logger.log("Starting repeatAdd test...");
  const addResult = await repeatAddProcessor.addExpensesFromAllRepeatAdd(
    "mJrkPOf5AthGokZEG3uufSpqn9E3"
  );
  res.send("repeatAdd test completed.");
});
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
    logger.log(`Start getting access token and refresh token...`);
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

    /* Gmailアドレスを取得する */
    logger.log(`Start getting gmail....`);
    const userInfoRes = await axios.get(
      "https://www.googleapis.com/oauth2/v3/userinfo",
      {
        headers: {
          Authorization: `Bearer ${access_token}`,
        },
      }
    );
    const gmailEmail = userInfoRes.data.email;

    if (!gmailEmail) {
      throw new Error("Unable to get Gmail email address.");
    }

    /* ここでFirebaseのGmailと一致しているかチェックし、一致していなかったら弾く */
    const userRecord = await admin.auth().getUser(uid);
    const userEmail = userRecord.email;
    if (gmailEmail !== userEmail) {
      throw new Error("Permitted email and user email is different");
    }

    /**
     * refresh_tokenを暗号化して保存する
     */

    /* ここでMailboxGmailTokenTypeに変換しないとだめ */
    const mailboxToken: MailboxGmailTokenType = {
      refreshToken: refresh_token,
      gmail: gmailEmail,
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
    } else {
      logger.log(`Successfully set mailbox extraction token for user ${uid}.`);
    }
    res.send(
      `<h1>I'm God Akita.</h1><h2>Process finished.<br>Please close this window.</h2>`
    );
  } catch (err) {
    if (axios.isAxiosError(err)) {
      logger.error(
        "Axios error:",
        JSON.stringify(err.response?.data ?? err.message)
      );
    } else {
      logger.error("Unexpected error:", err);
    }
    res.status(200).send(`OAuth token exchange failed.`);
  }
});

const scheduledMailboxExtraction = async (mailTypeList: AllMailType[]) => {
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
    /* ユーザーごとにインスタンスを生成 */
    const mailboxExtrInstance = new MailboxExtractionProcessor(
      uid,
      mailboxExtractionService,
      expenseService,
      categoryService,
      categoryAssignmentService
    );
    /* ユーザーごとに実行 */
    await mailboxExtrInstance.processAllMailTypeList(mailTypeList);
  }
};

for (const [_, schedule] of mailboxExtractionSchedules.entries()) {
  /* この関数はあくまでスケジュールをdeployしているだけ */
  exports[`mailboxExtractionJob_${schedule.id}`] = onSchedule(
    {
      schedule: schedule.cron,
      timeZone: TriggerTimeZone,
      concurrency: 1,
    },
    async () => {
      await scheduledMailboxExtraction(schedule.mailTypes);
    }
  );
}

/**
 * 定期便リストの生成
 */
const amazonSubscribeMonitor = async () => {
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
    const processor = new AmazonSubscribeMonitorItemsProcessor(
      uid,
      mailboxExtractionService
    );
    const ret = await processor.handleAmazonSubscribeItems();
    if (ret.status !== FuncStatus.SUCCESS) {
      logger.error(`Failed to handle Amazon Subscribe items: ${ret.message ?? "No message"}`);
    }
  }
};

exports.daily_amazonSubscribeMonitorJob = onSchedule(
  {
    schedule: "0 8 * * *", // 毎日 8:00 JST
    timeZone: TriggerTimeZone, // 現在時刻の設定も日本にしているから、大丈夫。
    concurrency: 1,
  },
  async (_) => {
    logger.log("Starting daily amazonSubscribeMonitor job...");
    await amazonSubscribeMonitor();
  }
);