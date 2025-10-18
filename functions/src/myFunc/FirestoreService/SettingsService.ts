import { Firestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { TimeZone } from "../../constants/TimeZone";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { UserPreferences } from "../../type/UserPreferences";

export class SettingsService {
  private db: Firestore;

  constructor(db: Firestore) {
    this.db = db;
  }

  private getSettingsColRef(userId: string) {
    return this.db.collection("users").doc(userId).collection("settings");
  }

  private getUserPreferencesDocRef(userId: string) {
    return this.getSettingsColRef(userId).doc(
      "user_preferences"
    ); /* ノード名はURLフレンドリーの方が良い */
  }

  async setUserPreferences(
    userId: string,
    preferences: UserPreferences
  ): Promise<FuncResultWithData<UserPreferences>> {
    try {
      const docRef = this.getUserPreferencesDocRef(userId);
      await docRef.set(preferences, { merge: true });
      return {
        status: FuncStatus.SUCCESS,
        data: preferences,
        message: `User preferences for ${userId} added successfully.`,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to add user preferences for ${userId}: ${error.message}`,
      };
    }
  }

  async getUserPreferences(
    userId: string
  ): Promise<FuncResultWithData<UserPreferences>> {
    try {
      const docRef = this.getUserPreferencesDocRef(userId);
      const docSnapshot = await docRef.get();
      if (!docSnapshot.exists) {
        return {
          status: FuncStatus.ERROR,
          message: `User preferences for ${userId} do not exist.`,
        };
      }
      const raw = docSnapshot.data();
      if (!raw) {
        return {
          status: FuncStatus.ERROR,
          message: `User preferences for ${userId} are empty.`,
        };
      }

      /**
       * UserPreferncesの型を変更したときは
       * ここも変更する必要がある。
       * UserPreferencesの型が変わり、functions側の変更をし忘れたときのために
       * 一個ずつ入れていく。
       */
      if (typeof raw.timeZone !== "string") {
        /* ログに残しておくけど次に行く。 */
        logger.error(
          `Invalid timeZone format for user ${userId}. Expected string, got ${typeof raw.timeZone}.`
        );
      }
      const data: UserPreferences = {
        timeZone: raw.timeZone || TimeZone.JST, // デフォルト値を設定
      };

      return {
        status: FuncStatus.SUCCESS,
        data: data,
        message: `User preferences for ${userId} retrieved successfully.`,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to get user preferences for ${userId}: ${error.message}`,
      };
    }
  }

  async getUserTimeZone(userId: string): Promise<FuncResultWithData<string>> {
    const userPreferencesResult = await this.getUserPreferences(userId);
    if (userPreferencesResult.status !== FuncStatus.SUCCESS) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to retrieve user preferences for ${userId}: ${userPreferencesResult.message}`,
      };
    }

    /* userPreferencesを全部取って、その中からtimeZoneだけ抽出する */
    const timeZone = userPreferencesResult.data?.timeZone;
    if (!timeZone) {
      return {
        status: FuncStatus.ERROR,
        message: `Time zone not found in user preferences for ${userId}.`,
      };
    }

    return {
      status: FuncStatus.SUCCESS,
      data: timeZone,
      message: `User time zone for ${userId} retrieved successfully.`,
    };
  }
}
