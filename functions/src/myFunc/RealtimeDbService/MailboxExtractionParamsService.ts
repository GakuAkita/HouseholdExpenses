import { Database, Reference } from "firebase-admin/database";
import { logger } from "firebase-functions";
import {
  FuncResult,
  FuncResultWithData,
  FuncStatus,
} from "../../type/FuncStatus";
import { MailboxTokenType } from "../../type/Mailbox";
import { admin } from "../firebaseAdmin";
import { decryptWithKey, encryptWithKey } from "../utility/encryption";
admin;

export class MailboxExtractionService {
  private db: Database;

  constructor(db: Database) {
    this.db = db;
  }

  private getUserMailboxExtractionRef(userId: string): Reference {
    return this.db.ref("users").child(userId).child("mailbox_extraction");
  }

  private getUserMailboxExtractionTokenRef(userId: string): Reference {
    return this.getUserMailboxExtractionRef(userId).child("token");
  }

  /**
   * 単純にメールボックス取得のためのパラメータを保存
   */
  private async setMailboxExtractionToken(
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
      const snapshot = await ref.once("value");
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
}
