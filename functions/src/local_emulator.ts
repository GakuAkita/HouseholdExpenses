import * as dotenv from "dotenv";
import { logger } from "firebase-functions";
import * as path from "path";
import { RepeatFrequency } from "./constants/RepeatFrequency";
import { TimeZone } from "./constants/TimeZone";
import { GmailApiClient } from "./myFunc/Client/GmailApiClient";
import { ExpenseService } from "./myFunc/FirestoreService/ExpenseService";
import { FirestoreService } from "./myFunc/FirestoreService/FirestoreService";
import { RepeatAddService } from "./myFunc/FirestoreService/RepeatAddService";
import { SettingsService } from "./myFunc/FirestoreService/SettingsService";
import { UserService } from "./myFunc/FirestoreService/UserService";
import { RepeatAddProcessor } from "./myFunc/Processor/RepeatAddProcessor";
import { UserSettingsProcessor } from "./myFunc/Processor/UserSettingsProcessor";
import { decryptWithKey } from "./myFunc/utility/encryption";
import { Category } from "./type/Category";
import { Expense } from "./type/Expense";
import { FuncStatus } from "./type/FuncStatus";
import {
  BaseGoogleOAuthConfig,
  GoogleOAuthSecrets,
} from "./type/GoogleOAuthSecrets";
import { MailboxTokenType } from "./type/Mailbox";
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
const mailboxExtractionParamsService = new MailboxExtractionParamsService(db);

const repeatAddProcessor = new RepeatAddProcessor(
  repeatAddService,
  expenseService,
  settingsService
);

const userSettingsProcessor = new UserSettingsProcessor(
  userService,
  settingsService
);

const userId = "testUser"; // テスト用のユーザーID

const init_add = async () => {
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

  const set_ret = await settingsService.setUserPreferences(
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
// schedule_func();

/**
 * 引数にrefresh_tokenを受取、
 * 暗号化して保存する
 */
const storeRefreshToken = async (rawToken: string) => {
  if (!rawToken) {
    logger.debug("No raw token provided.");
    return;
  }
  const encryptionKey = process.env.ENCRYPTION_KEY;
  if (!encryptionKey) {
    console.error("Encryption key is not set in environment variables.");
    return;
  }

  const refreshTokenSamp = "akita_gaku";

  const tokenSet: MailboxTokenType = {
    refreshToken: refreshTokenSamp,
  };

  const tokenSetRet =
    await mailboxExtractionParamsService.setMailboxExtractionTokenWithEncryption(
      "testUser",
      tokenSet,
      encryptionKey
    );

  const getTokenRet =
    await mailboxExtractionParamsService.getMailboxExtractionTokenWithDecryption(
      "testUser",
      encryptionKey
    );
};

// storeRefreshToken("akita_gaku");

const getRefreshTokenTest = async () => {
  const config = process.env.GOOGLE_OAUTH_SECRETS;
  if (!config) {
    logger.error("Google OAuth secrets are not set in environment variables.");
    return;
  }

  const secrets: GoogleOAuthSecrets = JSON.parse(config);
  if (
    !secrets.clientId ||
    !secrets.clientSecret ||
    !secrets.redirectUri ||
    !secrets.encryptionKey
  ) {
    logger.error("Google OAuth secrets are incomplete.");
    return;
  }

  const refreshTokenEncry = process.env.ENCRYPTED_REFRESH_TOKEN;
  if (!refreshTokenEncry) {
    logger.error("Unable to get encrypted refresh token.");
    return;
  }
  const refreshToken = decryptWithKey(refreshTokenEncry, secrets.encryptionKey);
  if (!refreshToken) {
    logger.error("unable to decrypt");
    return;
  }

  const gmailConfig: BaseGoogleOAuthConfig = {
    clientId: secrets.clientId,
    clientSecret: secrets.clientSecret,
    refreshToken: refreshToken,
  };

  const gmailClient = new GmailApiClient(gmailConfig);
  const query =
    "in:inbox category:primary subject:楽天ペイアプリご利用内容確認メール";
  const queryResult = await gmailClient.queryMessages(query);
  if (queryResult.status != FuncStatus.SUCCESS) {
    logger.error(`${queryResult.message}`);
    return;
  }

  const msgIds = queryResult.data;
  if (!msgIds) {
    /* ここに来るはずはないが、、 */
    logger.error("data is undefined");
    return;
  }
  if (msgIds.length > 0) {
    const singleMsg = await gmailClient.getMessageBodyAsHtml(msgIds[1]);
    logger.info(singleMsg);
  }
};

getRefreshTokenTest();
