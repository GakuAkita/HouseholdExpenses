import { logger } from "firebase-functions";
import { FuncStatus } from "../../type/FuncStatus";
import { UserData } from "../../type/UserData";
import { defaultUserPreferences } from "../../type/UserPreferences";
import { UserRTDbService } from "../RealtimeDbService/UserRTDbService";
import { SettingsService } from "./../FirestoreService/SettingsService";
import { UserService } from "./../FirestoreService/UserService";
export class UserSettingsProcessor {
  constructor(
    private userService: UserService,
    private userRTDbService: UserRTDbService,
    private settingsService: SettingsService
  ) {}

  async setInitialUserSettings(userId: string, email: string) {
    /* これでまずコレクションを作成する */
    const userData: UserData = {
      id: userId,
      email: email,
    };

    /* idとemailをセット */
    let ret = await this.userService.setUserData(userId, userData);
    if (ret.status != FuncStatus.SUCCESS) {
      logger.error(ret.message);
    }

    ret = await this.userRTDbService.setUserData(userId, userData);
    if (ret.status != FuncStatus.SUCCESS) {
      logger.error(ret.message);
    }

    /* デフォルトのUserPrefrencesをセット */
    ret = await this.settingsService.setUserPreferences(
      userId,
      defaultUserPreferences
    );

    if (ret.status != FuncStatus.SUCCESS) {
      logger.error(ret.message);
    }
  }
}
