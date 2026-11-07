import { readFileSync } from "fs";
import { join } from "path";
import * as admin from "firebase-admin";
import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { createTestServices, deleteTestUser, idTokenOf, newTestUserId } from "./emulator";

/*
 * Checks firestore.rules in the Firestore emulator. The rules are loaded through the emulator's
 * REST API, so they don't depend on firebase.json. Other tests use the Admin SDK, which ignores
 * the rules. Requests go through the REST API as a signed-in user, like the app.
 */

const host = process.env.FIRESTORE_EMULATOR_HOST as string;
const projectId = process.env.GCLOUD_PROJECT as string;
const rulesText = readFileSync(join(__dirname, "../../../firestore.rules"), "utf8");

const documentUrl = (path: string) =>
  `http://${host}/v1/projects/${projectId}/databases/(default)/documents/${path}`;

type Result = "allowed" | "denied";
const resultOf = async (response: Response): Promise<Result> => {
  /* A read of a missing document that the rules allow is 404. */
  if (response.ok || response.status === 404) return "allowed";
  if (response.status === 401 || response.status === 403) return "denied";
  throw new Error(`Unexpected status ${response.status}: ${await response.text()}`);
};

const authHeader = (token?: string): Record<string, string> =>
  token ? { Authorization: `Bearer ${token}` } : {};

const write = async (path: string, token?: string): Promise<Result> =>
  resultOf(
    await fetch(documentUrl(path), {
      method: "PATCH",
      headers: { "Content-Type": "application/json", ...authHeader(token) },
      body: JSON.stringify({ fields: { name: { stringValue: "x" } } }),
    })
  );

const read = async (path: string, token?: string): Promise<Result> =>
  resultOf(await fetch(documentUrl(path), { headers: authHeader(token) }));

const remove = async (path: string, token?: string): Promise<Result> =>
  resultOf(await fetch(documentUrl(path), { method: "DELETE", headers: authHeader(token) }));

describe("firestore.rules", () => {
  /* Initializes firebase-admin for the Auth emulator. */
  createTestServices();
  const userId = newTestUserId();
  const otherUserId = newTestUserId();
  let token: string;
  let otherToken: string;

  beforeAll(async () => {
    const response = await fetch(`http://${host}/emulator/v1/projects/${projectId}:securityRules`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ rules: { files: [{ name: "firestore.rules", content: rulesText }] } }),
    });
    expect(response.ok, await response.text()).toBe(true);

    token = await idTokenOf(userId);
    otherToken = await idTokenOf(otherUserId);
  });

  afterAll(async () => {
    await deleteTestUser(userId);
    await deleteTestUser(otherUserId);
  });

  describe("the user", () => {
    it.each(["users/<self>", "users/<self>/expenses/e1", "users/<self>/settings/time_zone"])(
      "reads, writes and deletes %s",
      async (template) => {
        const path = template.replace("<self>", userId);
        expect(await write(path, token)).toBe("allowed");
        expect(await read(path, token)).toBe("allowed");
        expect(await remove(path, token)).toBe("allowed");
      }
    );
  });

  describe("another user", () => {
    it("can't read or write the user's documents", async () => {
      await admin.firestore().doc(`users/${userId}/expenses/e2`).set({ name: "x" });
      expect(await read(`users/${userId}/expenses/e2`, otherToken)).toBe("denied");
      expect(await write(`users/${userId}/expenses/e2`, otherToken)).toBe("denied");
      expect(await remove(`users/${userId}/expenses/e2`, otherToken)).toBe("denied");
    });
  });

  describe("someone signed out", () => {
    it("can't read or write anything", async () => {
      expect(await read(`users/${userId}/expenses/e2`)).toBe("denied");
      expect(await write(`users/${userId}/expenses/e3`)).toBe("denied");
    });
  });

  it("denies documents outside users/", async () => {
    expect(await read("other/doc", token)).toBe("denied");
    expect(await write("other/doc", token)).toBe("denied");
  });
});
