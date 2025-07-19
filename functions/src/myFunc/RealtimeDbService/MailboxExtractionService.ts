import { Database, Reference } from "firebase-admin/database";
import { logger } from "firebase-functions";
import {
  FuncResult,
  FuncResultWithData,
  FuncStatus,
} from "../../type/FuncStatus";
import {
  AllMailType,
  createRakutenPaySettingInstance,
  LastMailboxExtractionExec,
  MailboxTokenType,
  RakutenPaySetting,
} from "../../type/Mailbox";
import { admin } from "../firebaseAdmin";
import { decryptWithKey, encryptWithKey } from "../utility/encryption";
admin;

/**
 * あくまでRealtime Databaseとのやりとりのみに務める
 */
export class MailboxExtractionService {
  private db: Database;

  constructor(db: Database) {
    this.db = db;
  }

  private getUserMailboxExtractionRef(userId: string): Reference {
    return this.db.ref("users").child(userId).child("mailbox_extraction");
  }

  private getUserMailboxExtractionTokenRef(userId: string): Reference {
    return this.getUserMailboxExtractionRef(userId).child("gmail_token");
  }

  private getUserMailboxExtractionLastExecRef(userId: string): Reference {
    return this.getUserMailboxExtractionRef(userId).child("last_exec");
  }

  /* なんかいい名前がないな～ */
  private getUserMailboxExtractionLastExecSingleRef(
    userId: string,
    type: AllMailType
  ): Reference {
    const nodeName = type.nodeName;
    return this.getUserMailboxExtractionLastExecRef(userId).child(nodeName);
  }

  private getUserMailboxExtractionMailTypeSettingsRef(
    userId: string
  ): Reference {
    return this.getUserMailboxExtractionRef(userId).child("mail_type_settings");
  }

  private getUserMailboxExtractionSingleMailTypeSettingRef(
    userId: string,
    setting: AllMailType
  ): Reference {
    const nodeName = setting.nodeName;
    return this.getUserMailboxExtractionMailTypeSettingsRef(userId).child(
      nodeName
    );
  }

  /**
   * 単純にメールボックス取得のためのパラメータを保存
   */
  async setMailboxExtractionToken(
    userId: string,
    token: MailboxTokenType
  ): Promise<FuncResult> {
    try {
      const ref = this.getUserMailboxExtractionTokenRef(userId);
      const now = new Date();
      const isoString = now.toISOString();
      await ref.set({ ...token, timestamp: isoString });
      return {
        status: FuncStatus.SUCCESS,
        message: `Successfully set MailboxExtraction token.`,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to add user preferences for ${userId}: ${error.message}`,
      };
    }
  }

  /**
   * 実際に外側で使うときはこの関数を使う
   * 暗号化したものを保存!!
   */
  async setMailboxExtractionTokenWithEncryption(
    userId: string,
    rawToken: MailboxTokenType,
    encryptionKey: string
  ): Promise<FuncResult> {
    const encryptedRefreshToken = encryptWithKey(
      rawToken.refreshToken,
      encryptionKey
    );
    const encryptedToken: MailboxTokenType = {
      ...rawToken,
      refreshToken: encryptedRefreshToken,
    };
    logger.debug(
      `Setting mailbox extraction token for user ${userId} with encrypted refresh token.`
    );
    return this.setMailboxExtractionToken(userId, encryptedToken);
  }

  /**
   * 単純にトークンを取得してくる。
   */
  private async getMailboxExtractionToken(
    userId: string
  ): Promise<FuncResultWithData<MailboxTokenType>> {
    const ref = this.getUserMailboxExtractionTokenRef(userId);
    try {
      const snapshot = await ref.get();
      const data: MailboxTokenType | null = snapshot.val();

      if (!data || !data.refreshToken) {
        return {
          status: FuncStatus.ERROR,
          message: `data type doesn't match with expected type. user:${userId}`,
        };
      }

      return {
        status: FuncStatus.SUCCESS,
        message: `Successfully retrieved MailboxExtraction token.`,
        data: data,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to retrieve MailboxExtraction token for user ${userId}: ${error.message}`,
      };
    }
  }

  async getMailboxExtractionTokenWithDecryption(
    userId: string,
    encryptionKey: string
  ): Promise<FuncResultWithData<MailboxTokenType>> {
    const ret = await this.getMailboxExtractionToken(userId);
    if (ret.status !== FuncStatus.SUCCESS) {
      /**
       * EMPTYの場合もすぐ返される!
       * なので、呼び出し側でエラーなのか、取れたけど空だったのか区別はできる
       */
      return ret;
    }
    const token = ret.data;
    if (!token || !token.refreshToken) {
      return {
        status: FuncStatus.ERROR,
        message: `No mailbox extraction token found for user ${userId}.`,
      };
    }

    /* 確実にトークンがあるので解読する */
    try {
      const decryptedRefreshToken = decryptWithKey(
        token.refreshToken,
        encryptionKey
      );
      return {
        status: FuncStatus.SUCCESS,
        message: `Successfully retrieved and decrypted MailboxExtraction token for user ${userId}.`,
        data: { ...token, refreshToken: decryptedRefreshToken },
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to decrypt MailboxExtraction token for user ${userId}: ${error.message}`,
      };
    }
  }

  /**
   * 各メールタイプの最終実行情報を取得
   */
  async setMailboxExtractionLastExec(
    userId: string,
    type: AllMailType /* 空のインスタンスでも良い */,
    lastExec: LastMailboxExtractionExec
  ): Promise<FuncResult> {
    try {
      /* typeに定義してあるnodeNameを用いてrefを決定 */
      const ref = this.getUserMailboxExtractionLastExecSingleRef(userId, type);

      await ref.set(lastExec);
      return {
        status: FuncStatus.SUCCESS,
        message: "Successfully set last exec",
      };
    } catch (e: any) {
      return {
        status: FuncStatus.ERROR,
        message: `setMAilboxExtractionLastExec failed:${e.message}`,
      };
    }
  }

  async getMailboxExtractionLastExec(
    userId: string,
    type: AllMailType
  ): Promise<FuncResultWithData<LastMailboxExtractionExec>> {
    try {
      const ref = this.getUserMailboxExtractionLastExecSingleRef(userId, type);
      const snapshot = await ref.get();
      const data: LastMailboxExtractionExec | null =
        snapshot.val(); /* nullなのか型変換ミスなのか見分けづらいかも。 */

      if (data == null) {
        return {
          status: FuncStatus.SUCCESS,
          message: `${type.nodeName} is not executed yet.`,
        };
      }

      return {
        status: FuncStatus.SUCCESS,
        message: `getMailboxExtractionLastExec Success`,
        data: data,
      };
    } catch (e: any) {
      return {
        status: FuncStatus.ERROR,
        message: `getMailboxExtractionLastExec failed: ${e.message}`,
      };
    }
  }

  /**
   * 各メールタイプの設定を取得する
   */
  async getMailboxExtractionMailTypeSetting(
    userId: string,
    type: AllMailType
  ): Promise<FuncResultWithData<AllMailType>> {
    try {
      const ref = this.getUserMailboxExtractionSingleMailTypeSettingRef(
        userId,
        type
      );
      const snapshot = await ref.get(); //ここで止まっているな
      const data: AllMailType | null = snapshot.val();
      if (data == null) {
        return {
          status: FuncStatus.EMPTY,
          message: "Data was null",
        };
      }
      return {
        status: FuncStatus.SUCCESS,
        message: "Successfully get data",
        data: data,
      };
    } catch (e: any) {
      return {
        status: FuncStatus.ERROR,
        message: `getMailboxExtractionMailTypeSetting failed:${e.message}`,
      };
    }
  }

  async setMailboxExtractionMailTypeSetting(
    userId: string,
    setting: AllMailType
  ): Promise<FuncResult> {
    try {
      /* typeに定義してあるnodeNameを用いてrefを決定 */
      const ref = this.getUserMailboxExtractionSingleMailTypeSettingRef(
        userId,
        setting
      );

      await ref.set(setting);
      return {
        status: FuncStatus.SUCCESS,
        message: "Successfully set last exec",
      };
    } catch (e: any) {
      return {
        status: FuncStatus.ERROR,
        message: `setMAilboxExtractionLastExec failed:${e.message}`,
      };
    }
  }

  /**
   * 楽天Payの設定を取り出す（ラップしているだけ）
   */
  async getRakutenPaySetting(
    userId: string
  ): Promise<FuncResultWithData<RakutenPaySetting>> {
    const rakuntePaySample = createRakutenPaySettingInstance();

    const ret = await this.getMailboxExtractionMailTypeSetting(
      userId,
      rakuntePaySample
    );

    if (ret.status != FuncStatus.SUCCESS) {
      return {
        status: ret.status,
        message: ret.message,
      };
    }

    const data = ret.data;
    if (data?.nodeName == rakuntePaySample.nodeName) {
      return {
        status: ret.status,
        message: ret.message,
        data: data as RakutenPaySetting,
      };
    } else {
      /* ここに来ることはないけどな、、 */
      return {
        status: FuncStatus.ERROR,
        message: "Returned mail type was not RakutenPay",
      };
    }
  }
  /** 楽天Payのサンプル
   * const sampleRakuten: RakutenPaySetting = createRakutenPaySettingInstance({
      enabled: true,
      storeCategoryAssignments: {
        asdfsdfsa: {
          id: "asdfsdfsa",
          categoryId: "category1",
          name: "ローソン",
          condition: "contains",
        },
      },
   */
}
