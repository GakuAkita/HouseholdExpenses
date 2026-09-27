import { Firestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { TimeZone } from "../../constants/TimeZone";
import { UserPreferences } from "../../type/UserPreferences";

export class SettingsService {
  constructor(private db: Firestore) {}

  private getUserPreferencesDocRef(userId: string) {
    /* ノード名はURLフレンドリーの方が良い */
    return this.db.collection("users").doc(userId).collection("settings").doc("user_preferences");
  }

  async setUserPreferences(userId: string, preferences: UserPreferences): Promise<void> {
    await this.getUserPreferencesDocRef(userId).set(preferences, { merge: true });
  }

  /** The user's preferences, or null when the user has none. */
  async getUserPreferences(userId: string): Promise<UserPreferences | null> {
    const snapshot = await this.getUserPreferencesDocRef(userId).get();
    const raw = snapshot.data();
    if (!snapshot.exists || !raw) {
      return null;
    }

    /**
     * UserPreferncesの型を変更したときはここも変更する必要がある。
     * 型が変わり、functions側の変更をし忘れたときのために一個ずつ入れていく。
     */
    if (typeof raw.timeZone !== "string") {
      /* ログに残しておくけど次に行く。 */
      logger.error(
        `Invalid timeZone format for user ${userId}. Expected string, got ${typeof raw.timeZone}.`
      );
    }
    return {
      timeZone: raw.timeZone || TimeZone.JST, // デフォルト値を設定
    };
  }

  /** The user's time zone. Throws when the user has no preferences. */
  async getUserTimeZone(userId: string): Promise<string> {
    const preferences = await this.getUserPreferences(userId);
    if (!preferences) {
      throw new Error(`User preferences for ${userId} do not exist.`);
    }
    return preferences.timeZone;
  }
}
