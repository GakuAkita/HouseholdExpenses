import * as admin from "firebase-admin";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { GoogleOAuthApi } from "../../src/infra/gmail/GoogleOAuthApi";
import { deleteUserData } from "../../src/jobs/deleteUserData";
import { disconnectGmail } from "../../src/jobs/gmailDisconnect";
import { encryptWithKey } from "../../src/shared/encryption";
import { createTestServices, deleteTestUser, newTestUserId, testGmailOf, testSecrets } from "./emulator";

describe("Gmail disconnect and deleted users' data", () => {
  const userId = newTestUserId();
  const otherUserId = newTestUserId();
  const gmail = testGmailOf(userId);

  const revokeToken = vi.fn<GoogleOAuthApi["revokeToken"]>();
  const services = createTestServices({
    googleOAuthApi: {
      exchangeCode: async () => ({ accessToken: undefined, refreshToken: undefined }),
      fetchEmail: async () => undefined,
      revokeToken,
    },
  });

  const mailbox = (uid: string) => admin.database().ref(`users/${uid}/mailbox_extraction`);

  /** Data in both databases, including a subcollection and a Gmail token. */
  const writeUserData = async (uid: string) => {
    await admin.firestore().doc(`users/${uid}`).set({ id: uid });
    await admin.firestore().doc(`users/${uid}/expenses/expense1`).set({ amount: 100 });
    await admin.firestore().doc(`users/${uid}/settings/user_preferences`).set({ theme: "dark" });
    await mailbox(uid).child("email_template_settings/rakuten_pay").set({ enabled: true });
    await services.mailboxExtractionService.saveGmailToken(
      uid,
      { refreshToken: `raw-token-of-${uid}`, gmail: testGmailOf(uid) },
      testSecrets.encryptionKey
    );
  };

  const hasFirestoreData = async (uid: string) =>
    (await admin.firestore().doc(`users/${uid}/expenses/expense1`).get()).exists;
  const hasRealtimeData = async (uid: string) => (await admin.database().ref(`users/${uid}`).get()).exists();
  const hasToken = async (uid: string) => (await mailbox(uid).child("gmail_tokens").get()).exists();

  beforeEach(async () => {
    revokeToken.mockReset();
    revokeToken.mockResolvedValue("revoked");
    await admin.auth().createUser({ uid: userId, email: gmail });
    await admin.auth().createUser({ uid: otherUserId, email: testGmailOf(otherUserId) });
    await writeUserData(userId);
    await writeUserData(otherUserId);
  });
  afterEach(async () => {
    await deleteTestUser(userId);
    await deleteTestUser(otherUserId);
  });

  describe("disconnectGmail", () => {
    it("revokes and removes only the token, and keeps the user's other data", async () => {
      await disconnectGmail(services, userId);

      expect(revokeToken.mock.calls).toEqual([[`raw-token-of-${userId}`]]);
      expect(await hasToken(userId)).toBe(false);
      expect(await services.mailboxExtractionService.getGmailToken(userId, testSecrets.encryptionKey)).toBeNull();
      expect(await hasFirestoreData(userId)).toBe(true);
      expect((await mailbox(userId).child("email_template_settings").get()).exists()).toBe(true);
    });

    it("keeps the token when revoking fails", async () => {
      revokeToken.mockRejectedValue(new Error("no response"));

      await expect(disconnectGmail(services, userId)).rejects.toThrow("no response");

      expect(await hasToken(userId)).toBe(true);
    });

    it("doesn't touch another user's token", async () => {
      await disconnectGmail(services, userId);

      expect(await hasToken(otherUserId)).toBe(true);
    });
  });

  describe("deleteUserData", () => {
    it("revokes the token and removes all the data of a user who no longer has an Auth record", async () => {
      /* No Auth user is created, so no trigger in the Functions emulator touches this data. */
      const deletedUserId = newTestUserId();
      await admin.firestore().doc(`users/${deletedUserId}`).set({ id: deletedUserId });
      await admin.firestore().doc(`users/${deletedUserId}/expenses/expense1`).set({ amount: 100 });
      await admin.firestore().doc(`users/${deletedUserId}/settings/user_preferences`).set({ theme: "dark" });
      await mailbox(deletedUserId)
        .child("gmail_tokens/someone__at__gmail__dot__com")
        .set({ refreshToken: encryptWithKey("raw-token", testSecrets.encryptionKey) });
      await mailbox(deletedUserId).child("email_template_settings/rakuten_pay").set({ enabled: true });

      await deleteUserData(services, deletedUserId);

      expect(revokeToken.mock.calls).toEqual([["raw-token"]]);
      expect(await hasFirestoreData(deletedUserId)).toBe(false);
      expect((await admin.firestore().doc(`users/${deletedUserId}/settings/user_preferences`).get()).exists).toBe(false);
      expect((await admin.firestore().doc(`users/${deletedUserId}`).get()).exists).toBe(false);
      expect(await hasRealtimeData(deletedUserId)).toBe(false);
    });

    it("keeps everything when revoking fails, so a retry can still revoke the token", async () => {
      revokeToken.mockRejectedValue(new Error("no response"));

      await expect(deleteUserData(services, userId)).rejects.toThrow("no response");

      expect(await hasFirestoreData(userId)).toBe(true);
      expect(await hasToken(userId)).toBe(true);
    });

    it("can run twice", async () => {
      await deleteUserData(services, userId);
      await deleteUserData(services, userId);

      expect(await hasFirestoreData(userId)).toBe(false);
      expect(await hasToken(userId)).toBe(false);
    });

    it("doesn't touch another user's data", async () => {
      await deleteUserData(services, userId);

      expect(await hasFirestoreData(otherUserId)).toBe(true);
      expect(await hasToken(otherUserId)).toBe(true);
    });
  });
});
