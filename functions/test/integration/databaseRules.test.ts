import { readFileSync } from "fs";
import { join } from "path";
import * as admin from "firebase-admin";
import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { createTestServices, idTokenOf, newTestUserId } from "./emulator";

/*
 * Checks database.rules.json in the Realtime Database emulator. The rules are loaded into their own
 * namespace, so they don't depend on firebase.json and don't change the namespace other tests use.
 * Requests go through the REST API as a signed-in user, like the app.
 */

const host = process.env.FIREBASE_DATABASE_EMULATOR_HOST as string;
const namespace = `${process.env.GCLOUD_PROJECT}-rules-test`;
const rulesText = readFileSync(join(__dirname, "../../../database.rules.json"), "utf8");

const url = (path: string, auth?: string) =>
  `http://${host}/${path}.json?ns=${namespace}${auth ? `&auth=${auth}` : ""}`;

/** The emulator accepts "owner" as an admin token, which ignores the rules. */
const asOwner = { Authorization: "Bearer owner" };

type Result = "allowed" | "denied";
const resultOf = async (response: Response): Promise<Result> => {
  if (response.ok) return "allowed";
  if (response.status === 401 || response.status === 403) return "denied";
  throw new Error(`Unexpected status ${response.status}: ${await response.text()}`);
};

const write = async (path: string, value: unknown, token?: string): Promise<Result> =>
  resultOf(await fetch(url(path, token), { method: "PUT", body: JSON.stringify(value) }));

const read = async (path: string, token?: string): Promise<Result> => resultOf(await fetch(url(path, token)));

const remove = async (path: string, token?: string): Promise<Result> =>
  resultOf(await fetch(url(path, token), { method: "DELETE" }));

describe("database.rules.json", () => {
  /* Initializes firebase-admin for the Auth emulator. */
  createTestServices();
  const userId = newTestUserId();
  const otherUserId = newTestUserId();
  let token: string;
  let otherToken: string;

  const user = `users/${userId}`;
  const mailbox = `${user}/mailbox_extraction`;
  const validRequest = () => ({
    nonce: "N".repeat(43),
    codeVerifier: "V".repeat(43),
    createdAt: { ".sv": "timestamp" },
  });

  beforeAll(async () => {
    const response = await fetch(`http://${host}/.settings/rules.json?ns=${namespace}`, {
      method: "PUT",
      headers: asOwner,
      body: rulesText,
    });
    expect(response.ok, await response.text()).toBe(true);

    token = await idTokenOf(userId);
    otherToken = await idTokenOf(otherUserId);

    /* Data that only Cloud Functions write. */
    await fetch(url(mailbox), {
      method: "PUT",
      headers: asOwner,
      body: JSON.stringify({
        gmail_tokens: { someone__at__gmail__dot__com: { refreshToken: "v2:encrypted", gmail: "someone@gmail.com" } },
        last_exec: { rakuten: { timestamp: 1 } },
        amazon_subscribe_monitor: { last_exec: { timestamp: 1 } },
      }),
    });
  });

  afterAll(async () => {
    await fetch(url("users"), { method: "DELETE", headers: asOwner });
    await admin.auth().deleteUsers([userId, otherUserId]);
  });

  describe("the user", () => {
    it("reads their own data, including whether Gmail is connected", async () => {
      expect(await read(user, token)).toBe("allowed");
      expect(await read(`${mailbox}/gmail_tokens/someone__at__gmail__dot__com`, token)).toBe("allowed");
    });

    it.each([
      [`email_template_settings/rakuten`, { enabled: true }],
      [`amazon_subscribe_monitor/subscribe_items/item1`, { name: "water" }],
    ])("writes mailbox_extraction/%s", async (path, value) => {
      expect(await write(`${mailbox}/${path}`, value, token)).toBe("allowed");
    });

    it("writes and removes category assignments", async () => {
      expect(await write(`${user}/category_assignment_data/product/a1`, { name: "x" }, token)).toBe("allowed");
      expect(await remove(`${user}/category_assignment_data/product/a1`, token)).toBe("allowed");
    });

    it.each([
      ["gmail_tokens", `${mailbox}/gmail_tokens/someone__at__gmail__dot__com`, { refreshToken: "mine" }],
      ["the whole gmail_tokens", `${mailbox}/gmail_tokens`, null],
      ["last_exec", `${mailbox}/last_exec/rakuten`, { timestamp: 0 }],
      ["amazon_subscribe_monitor/last_exec", `${mailbox}/amazon_subscribe_monitor/last_exec`, { timestamp: 0 }],
      ["the whole mailbox_extraction", mailbox, null],
      ["their whole user node", user, { anything: 1 }],
      ["a path the app doesn't use", `${user}/something_else`, 1],
    ])("can't write %s", async (_, path, value) => {
      expect(await write(path, value, token)).toBe("denied");
    });
  });

  describe("gmail_oauth_request", () => {
    const path = `${mailbox}/gmail_oauth_request`;

    it("accepts what the app writes, with the server time", async () => {
      expect(await write(path, validRequest(), token)).toBe("allowed");
      expect(await remove(path, token)).toBe("allowed");
    });

    it.each([
      ["a client clock time", { ...validRequest(), createdAt: Date.now() }],
      ["a short nonce", { ...validRequest(), nonce: "short" }],
      ["a nonce with a dot", { ...validRequest(), nonce: `${"N".repeat(42)}.` }],
      ["a short code verifier", { ...validRequest(), codeVerifier: "V".repeat(42) }],
      ["a missing field", { nonce: "N".repeat(43), createdAt: { ".sv": "timestamp" } }],
      ["an extra field", { ...validRequest(), uid: "someone" }],
    ])("rejects %s", async (_, value) => {
      expect(await write(path, value, token)).toBe("denied");
    });
  });

  describe("other users", () => {
    it("can't read or write the user's data", async () => {
      expect(await read(user, otherToken)).toBe("denied");
      expect(await read(`${mailbox}/gmail_tokens`, otherToken)).toBe("denied");
      expect(await write(`${mailbox}/gmail_oauth_request`, validRequest(), otherToken)).toBe("denied");
      expect(await write(`${mailbox}/email_template_settings/rakuten`, { enabled: false }, otherToken)).toBe(
        "denied"
      );
    });
  });

  describe("signed-out requests", () => {
    it("can't read or write anything", async () => {
      expect(await read(user)).toBe("denied");
      expect(await read("users")).toBe("denied");
      expect(await write(`${mailbox}/email_template_settings/rakuten`, { enabled: true })).toBe("denied");
    });
  });
});
