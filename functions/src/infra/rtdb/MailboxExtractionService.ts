import { timingSafeEqual } from "crypto";
import { Auth } from "firebase-admin/auth";
import { Database, Reference } from "firebase-admin/database";
import { Clock } from "../../shared/clock";
import { decryptWithKey, encryptWithKey, isLegacyCiphertext } from "../../shared/encryption";
import { RuntimeConfig } from "../../shared/runtimeConfig";
import {
  AllMailType,
  AmazonSubscribeItem,
  GmailOAuthRequest,
  LastMailboxExtractionExec,
  MailboxGmailTokenType,
  RakutenCardExcludeItem,
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
    return this.gmailTokensRef(userId).child(sanitizeEmail(gmail));
  }

  private gmailTokensRef(userId: string): Reference {
    return this.mailboxExtractionRef(userId).child("gmail_tokens");
  }

  /** Written by the app when it opens Google's consent page. See takeGmailOAuthRequest. */
  private gmailOAuthRequestRef(userId: string): Reference {
    return this.mailboxExtractionRef(userId).child("gmail_oauth_request");
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
    const refreshToken = decryptWithKey(token.refreshToken, encryptionKey);

    /* Tokens saved in the old AES-CBC format are saved again in the current format. */
    if (isLegacyCiphertext(token.refreshToken)) {
      await snapshot.ref.child("refreshToken").set(encryptWithKey(refreshToken, encryptionKey));
    }
    return { ...token, refreshToken };
  }

  /**
   * Every refresh token stored for the user, still encrypted. Empty when the user hasn't connected Gmail.
   * Doesn't look up the user's email, so it also works after the Auth user is deleted.
   */
  async getEncryptedGmailRefreshTokens(userId: string): Promise<string[]> {
    const tokens: Record<string, Partial<MailboxGmailTokenType>> | null = (await this.gmailTokensRef(userId).get()).val();
    return Object.values(tokens ?? {}).flatMap((token) => (token.refreshToken ? [token.refreshToken] : []));
  }

  async removeGmailTokens(userId: string): Promise<void> {
    await this.gmailTokensRef(userId).remove();
  }

  /* ****************************** Gmail OAuth request ****************************** */

  /**
   * Removes and returns the user's pending OAuth request when its nonce matches.
   * Returns null when there is no request or the nonce is different; a wrong nonce leaves the request.
   * A request can be taken only once, even by concurrent calls.
   */
  async takeGmailOAuthRequest(userId: string, nonce: string): Promise<GmailOAuthRequest | null> {
    let taken: GmailOAuthRequest | null = null;
    const result = await this.gmailOAuthRequestRef(userId).transaction((current: GmailOAuthRequest | null) => {
      taken = null;
      /*
       * The first call can get null before the server's value is known. Writing null then is
       * harmless: if the server has a value, the transaction runs again with it.
       */
      if (current === null) {
        return null;
      }
      if (typeof current.nonce !== "string" || !sameString(current.nonce, nonce)) {
        return undefined; /* abort: leave the request */
      }
      taken = current;
      return null;
    });
    return result.committed ? taken : null;
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

  /* ****************************** Rakuten Card exclude items ****************************** */

  /** The stores the user excluded, by id. Empty when the user has none. */
  async getRakutenCardExcludeItems(userId: string): Promise<Record<string, RakutenCardExcludeItem>> {
    return (await this.mailboxExtractionRef(userId).child("rakuten_card_exclude_items").get()).val() ?? {};
  }
}

/** Compares in constant time, so the comparison doesn't reveal how much of the nonce matched. */
const sameString = (a: string, b: string): boolean => {
  const bufA = Buffer.from(a);
  const bufB = Buffer.from(b);
  return bufA.length === bufB.length && timingSafeEqual(bufA, bufB);
};
