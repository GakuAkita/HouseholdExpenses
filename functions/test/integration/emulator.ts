import { randomUUID } from "crypto";
import * as admin from "firebase-admin";
import { initializeServices, ServiceOptions } from "../../src/myFunc/initializeServices";
import { GoogleOAuthSecretProvider } from "../../src/myFunc/googleOAuthSecrets";
import { systemClock } from "../../src/shared/clock";
import { FuncStatus } from "../../src/type/FuncStatus";
import { GoogleOAuthSecrets } from "../../src/type/GoogleOAuthSecrets";

/*
 * Helpers for tests against the Firebase emulators (see vitest.integration.config.ts).
 * Each test uses its own user id and deletes it afterwards, so tests don't see each other's data
 * or data you created in the emulator by hand.
 */

const projectId = process.env.GCLOUD_PROJECT as string;

export const testSecrets: GoogleOAuthSecrets = {
  clientId: "test-client-id",
  clientSecret: "test-client-secret",
  redirectUri: "http://localhost/callback",
  /* 32 bytes, base64 */
  encryptionKey: Buffer.alloc(32, 7).toString("base64"),
};

export const fakeSecretProvider: GoogleOAuthSecretProvider = {
  load: async () => ({ status: FuncStatus.SUCCESS, data: testSecrets }),
};

/** Services connected to the emulators. Nothing in them calls Secret Manager or Gmail. */
export const createTestServices = (options: ServiceOptions = {}) =>
  initializeServices({
    appOptions: {
      projectId,
      /* FIREBASE_DATABASE_EMULATOR_HOST redirects this URL to the emulator. */
      databaseURL: `https://${projectId}-default-rtdb.firebaseio.com`,
    },
    config: {
      isEmulator: false,
      projectId,
      emulatorGmail: undefined,
      emulatorGoogleOAuthSecrets: undefined,
    },
    clock: systemClock,
    secrets: fakeSecretProvider,
    createGmailClient: () => {
      throw new Error("This test doesn't fake Gmail. Pass createGmailClient.");
    },
    ...options,
  });

export const newTestUserId = () => `integration-${randomUUID()}`;

/** A Gmail address for the user. Auth rejects the same email for two users, so it is unique per user. */
export const testGmailOf = (userId: string) => `${userId}@gmail.com`;

/** Deletes everything the user has in Firestore, Realtime Database and Auth. */
export const deleteTestUser = async (userId: string) => {
  await admin.firestore().recursiveDelete(admin.firestore().doc(`users/${userId}`));
  await admin.database().ref(`users/${userId}`).remove();
  await admin
    .auth()
    .deleteUser(userId)
    .catch(() => {
      /* The test didn't create an Auth user. */
    });
};
