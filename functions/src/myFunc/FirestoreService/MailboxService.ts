import { Firestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import {
  FuncResult,
  FuncResultWithData,
  FuncStatus,
} from "../../type/FuncStatus";
import { MailboxTokenType } from "../../type/Mailbox";
import { decryptWithKey, encryptWithKey } from "../utility/encryption";

export class MailboxExtractionService {
  private db: Firestore;

  constructor(db: Firestore) {
    this.db = db;
  }

  /**
   * メールボックス取得のためのパラメータのコレクション
   */
  private getUserMailboxExtractionParamsColRef(userId: string) {
    return this.db
      .collection("users")
      .doc(userId)
      .collection("mailbox_extraction_params");
  }

  /**
   * 各メールタイプの設定コレクション
   */
  private getUserMailboxExtractionMailTypeSettingsColRef(userId: string) {
    return this.db
      .collection("users")
      .doc(userId)
      .collection("mailbox_extraction_mail_type_settings");
  }

  /**
   * パラメータを保存するdocumentの参照
   */
  private getUserMailboxExtractionParamsTokenDocRef(userId: string) {
    return this.getUserMailboxExtractionParamsColRef(userId).doc("token");
  }

  /**
   * 単純にメールボックス取得のためのパラメータを保存
   */
  private async setMailboxExtractionToken(
    userId: string,
    token: MailboxTokenType
  ): Promise<FuncResult> {
    try {
      const docRef = this.getUserMailboxExtractionParamsTokenDocRef(userId);
      await docRef.set(token, { merge: true });
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
    const encryptedKey = encryptWithKey(rawToken.refreshToken, encryptionKey);
    const encryptedToken: MailboxTokenType = {
      ...rawToken,
      refreshToken: encryptedKey,
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
    const docRef = this.getUserMailboxExtractionParamsTokenDocRef(userId);
    try {
      const snapShot = await docRef.get();
      if (!snapShot.exists) {
        return {
          status: FuncStatus.EMPTY,
          message: `MailboxExtraction token does not exist for user ${userId}.`,
        };
      }

      const data: MailboxTokenType = snapShot.data() as MailboxTokenType;
      if (!data || !data.refreshToken) {
        return {
          status: FuncStatus.ERROR,
          message: `data type doesn't match with expected type. user:${userId}`,
        };
      }

      /* ここまで来てようやく成功 */
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
