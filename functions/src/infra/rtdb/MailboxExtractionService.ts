import { Auth } from "firebase-admin/auth";
import { Database, Reference } from "firebase-admin/database";
import { Clock } from "../../shared/clock";
import { decryptWithKey, encryptWithKey } from "../../shared/encryption";
import { RuntimeConfig } from "../../shared/runtimeConfig";
import {
  AllMailType,
  AmazonSubscribeItem,
  LastMailboxExtractionExec,
  MailboxGmailTokenType,
} from "../../type/Mailbox";
import { sanitizeEmail } from "./emailEncode";

/**
 * Mailbox extraction data in the Realtime Database, under users/{userId}/mailbox_extraction.
 * Methods throw when the database call fails, and return null for data that doesn't exist.
 */
export class MailboxExtractionService {
  constructor(
    private db: Database,
    private deps: { auth: Auth; clock: Clock; config: RuntimeConfig }
  ) {}

  private mailboxExtractionRef(userId: string): Reference {
    return this.db.ref("users").child(userId).child("mailbox_extraction");
  }

  /**
   * gmailトークンは複数持てるようにしようとおもったけど、
   * そうするとLastExecとかも複数持たなきゃいけなくなりそうなので、
   * とりあえずは一つだけにしておく
   */
  private gmailTokenRef(userId: string, gmail: string): Reference {
    /* gmailには.や@が入っているので別文字の置き換える */
    return this.mailboxExtractionRef(userId).child("gmail_tokens").child(sanitizeEmail(gmail));
  }

  private lastExecRef(userId: string, type: AllMailType): Reference {
    return this.mailboxExtractionRef(userId).child("last_exec").child(type.nodeName);
  }

  private mailTypeSettingRef(userId: string, type: AllMailType): Reference {
    return this.mailboxExtractionRef(userId).child("email_template_settings").child(type.nodeName);
  }

  /**
   * 定期便キャンセルのlastExecと次回配送のlastExec分けたほうがいいのかな、、
   * 一旦一緒でいいか
   */
  private amazonSubscribeMonitorRef(userId: string): Reference {
    return this.mailboxExtractionRef(userId).child("amazon_subscribe_monitor");
  }

  private amazonSubscribeItemsRef(userId: string): Reference {
    return this.amazonSubscribeMonitorRef(userId).child("subscribe_items");
  }

  /** The Gmail address the token is stored under: the user's sign-in email (MY_GMAIL in the emulator). */
  private async gmailOf(userId: string): Promise<string> {
    const email = this.deps.config.isEmulator
      ? this.deps.config.emulatorGmail
      : (await this.deps.auth.getUser(userId)).email;
    if (!email) {
      throw new Error(`No email for user ${userId}`);
    }
    return email;
  }

  /* ****************************** Gmail token ****************************** */

  /** Saves the token with the refresh token encrypted, under the user's sign-in email. */
  async saveGmailToken(userId: string, token: MailboxGmailTokenType, encryptionKey: string): Promise<void> {
    const gmail = (await this.deps.auth.getUser(userId)).email;
    if (!gmail) {
      throw new Error("email can't be passed to Ref func. because it is empty");
    }
    await this.gmailTokenRef(userId, gmail).set({
      ...token,
      refreshToken: encryptWithKey(token.refreshToken, encryptionKey),
      timestamp: this.deps.clock.now().toISOString(),
    });
  }

  /** The token with the refresh token decrypted, or null when the user hasn't connected Gmail. */
  async getGmailToken(userId: string, encryptionKey: string): Promise<MailboxGmailTokenType | null> {
    const snapshot = await this.gmailTokenRef(userId, await this.gmailOf(userId)).get();
    if (!snapshot.exists()) {
      return null;
    }
    const token: MailboxGmailTokenType | null = snapshot.val();
    if (!token || !token.refreshToken) {
      throw new Error(`data type doesn't match with expected type. user:${userId}`);
    }
    return { ...token, refreshToken: decryptWithKey(token.refreshToken, encryptionKey) };
  }

  /* ****************************** Mail type settings ****************************** */

  /** The user's setting for the mail type, or null when the user hasn't set it. */
  async getMailTypeSetting(userId: string, type: AllMailType): Promise<AllMailType | null> {
    return (await this.mailTypeSettingRef(userId, type).get()).val();
  }

  async setMailTypeSetting(userId: string, setting: AllMailType): Promise<void> {
    await this.mailTypeSettingRef(userId, setting).set(setting);
  }

  /* ****************************** Last execution ****************************** */

  /** When the mail type was last processed, or null before the first run. */
  async getLastExec(userId: string, type: AllMailType): Promise<LastMailboxExtractionExec | null> {
    return (await this.lastExecRef(userId, type).get()).val();
  }

  async setLastExec(userId: string, type: AllMailType, lastExec: LastMailboxExtractionExec): Promise<void> {
    await this.lastExecRef(userId, type).set(lastExec);
  }

  /* ****************************** Amazon subscribe monitor ****************************** */

  async getAmazonSubscribeMonitorLastExec(userId: string): Promise<LastMailboxExtractionExec | null> {
    return (await this.amazonSubscribeMonitorRef(userId).child("last_exec").get()).val();
  }

  async setAmazonSubscribeMonitorLastExec(userId: string, lastExec: LastMailboxExtractionExec): Promise<void> {
    await this.amazonSubscribeMonitorRef(userId).child("last_exec").set(lastExec);
  }

  /** The user's subscribe items by id. Empty when the user has none. */
  async getAmazonSubscribeItems(userId: string): Promise<Record<string, AmazonSubscribeItem>> {
    return (await this.amazonSubscribeItemsRef(userId).get()).val() ?? {};
  }

  /** Adds the item with a new id and the current time. Returns the saved item. */
  async addAmazonSubscribeItem(userId: string, item: AmazonSubscribeItem): Promise<AmazonSubscribeItem> {
    const newRef = this.amazonSubscribeItemsRef(userId).push();
    if (!newRef.key) {
      throw new Error(`Failed to generate new key for subscribe item.`);
    }
    const saved: AmazonSubscribeItem = {
      ...item,
      id: newRef.key,
      timestamp: this.deps.clock.now().getTime(),
    };
    await newRef.set(saved);
    return saved;
  }

  async updateAmazonSubscribeItem(userId: string, item: AmazonSubscribeItem): Promise<void> {
    if (!item.id) {
      throw new Error(`There is no id in subscribeItem`);
    }
    await this.amazonSubscribeItemsRef(userId).child(item.id).update(item);
  }

  async removeAmazonSubscribeItem(userId: string, item: AmazonSubscribeItem): Promise<void> {
    if (!item.id) {
      throw new Error(`removeAmazonSubscribeItem: There is no id in subscribeItem`);
    }
    await this.amazonSubscribeItemsRef(userId).child(item.id).remove();
  }
}
