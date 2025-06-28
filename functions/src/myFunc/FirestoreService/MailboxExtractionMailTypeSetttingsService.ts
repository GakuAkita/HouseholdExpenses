import { Firestore } from "firebase-admin/firestore";

export class MailboxExtractionMailTypeSettingsService {
  private db: Firestore;

  constructor(db: Firestore) {
    this.db = db;
  }

  /**
   * メールボックス取得のためのパラメータのコレクション
   */
  private getUserMailboxExtractionMailTypeSettingsColRef(userId: string) {
    return this.db
      .collection("users")
      .doc(userId)
      .collection("mailbox_extraction_mail_type_settings");
  }
}
