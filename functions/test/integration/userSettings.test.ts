import * as admin from "firebase-admin";
import { afterEach, describe, expect, it } from "vitest";
import { createTestServices, deleteTestUser, newTestUserId } from "./emulator";

describe("initial user settings (onUserCreate)", () => {
  const userId = newTestUserId();
  const services = createTestServices();

  afterEach(() => deleteTestUser(userId));

  it("writes the user and the default preferences", async () => {
    await services.userSettingsProcessor.setInitialUserSettings(userId, "user@example.com");

    const user = await admin.firestore().doc(`users/${userId}`).get();
    expect(user.data()).toEqual({ id: userId, email: "user@example.com" });

    const preferences = await admin.firestore().doc(`users/${userId}/settings/user_preferences`).get();
    expect(preferences.data()).toEqual({ timeZone: "Asia/Tokyo" });

    const rtdbUser = await admin.database().ref(`users/${userId}`).get();
    expect(rtdbUser.val()).toEqual({ id: userId, email: "user@example.com" });
  });

  it("keeps other Realtime Database data of the user", async () => {
    await admin.database().ref(`users/${userId}/category_assignment_data/storeName/a`).set({ name: "x" });

    await services.userSettingsProcessor.setInitialUserSettings(userId, "user@example.com");

    const assignment = await admin.database().ref(`users/${userId}/category_assignment_data`).get();
    expect(assignment.val()).toEqual({ storeName: { a: { name: "x" } } });
  });
});
