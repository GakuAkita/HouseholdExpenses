import { logger } from "firebase-functions";
import { SettingsService } from "../infra/firestore/SettingsService";
import { UserService } from "../infra/firestore/UserService";
import { UserRTDbService } from "../infra/rtdb/UserRTDbService";
import { messageOf } from "../shared/errors";
import { UserData } from "../type/UserData";
import { defaultUserPreferences } from "../type/UserPreferences";

export class UserSettingsProcessor {
  constructor(
    private userService: Pick<UserService, "setUserData">,
    private userRTDbService: Pick<UserRTDbService, "setUserData">,
    private settingsService: Pick<SettingsService, "setUserPreferences">
  ) {}

  /**
   * Writes the new user's data and default preferences.
   * The writes are independent: a failed one is logged and the others still run.
   */
  async setInitialUserSettings(userId: string, email: string): Promise<void> {
    const userData: UserData = { id: userId, email };
    const steps: [string, () => Promise<void>][] = [
      /* これでまずコレクションを作成する */
      ["Firestore user", () => this.userService.setUserData(userId, userData)],
      ["Realtime Database user", () => this.userRTDbService.setUserData(userId, userData)],
      /* デフォルトのUserPrefrencesをセット */
      ["preferences", () => this.settingsService.setUserPreferences(userId, defaultUserPreferences)],
    ];
    for (const [name, step] of steps) {
      try {
        await step();
      } catch (error) {
        logger.error(`Failed to set ${name} for ${userId}: ${messageOf(error)}`);
      }
    }
  }
}
