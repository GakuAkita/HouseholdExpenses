import crypto from "crypto";
import { describe, expect, it } from "vitest";
import { decryptWithKey, encryptWithKey, isLegacyCiphertext } from "../../src/shared/encryption";

const key = Buffer.alloc(32, 7).toString("base64");
const otherKey = Buffer.alloc(32, 8).toString("base64");

/** How tokens were encrypted before v2 (AES-256-CBC, "<iv>:<ciphertext>"). */
const encryptLegacyCbc = (plainText: string, base64Key: string): string => {
  const iv = crypto.randomBytes(16);
  const cipher = crypto.createCipheriv("aes-256-cbc", Buffer.from(base64Key, "base64"), iv);
  const encrypted = Buffer.concat([cipher.update(plainText, "utf8"), cipher.final()]);
  return `${iv.toString("hex")}:${encrypted.toString("hex")}`;
};

/** Flips one bit of the hex part at the index (0: iv, 1: tag, 2: ciphertext). */
const tamper = (encrypted: string, part: number): string => {
  const parts = encrypted.split(":");
  const hex = parts[part + 1];
  const flipped = (parseInt(hex[0], 16) ^ 1).toString(16);
  parts[part + 1] = flipped + hex.slice(1);
  return parts.join(":");
};

describe("encryptWithKey / decryptWithKey", () => {
  it("round-trips with AES-GCM in the v2 format", () => {
    const encrypted = encryptWithKey("1//refresh-token", key);

    expect(encrypted).toMatch(/^v2:[0-9a-f]{24}:[0-9a-f]{32}:[0-9a-f]+$/);
    expect(isLegacyCiphertext(encrypted)).toBe(false);
    expect(decryptWithKey(encrypted, key)).toBe("1//refresh-token");
  });

  it("uses a new IV every time", () => {
    expect(encryptWithKey("same", key)).not.toBe(encryptWithKey("same", key));
  });

  it.each([
    ["IV", 0],
    ["auth tag", 1],
    ["ciphertext", 2],
  ])("rejects a changed %s", (_, part) => {
    const encrypted = encryptWithKey("1//refresh-token", key);
    expect(() => decryptWithKey(tamper(encrypted, part), key)).toThrow();
  });

  it("rejects the wrong key", () => {
    expect(() => decryptWithKey(encryptWithKey("1//refresh-token", key), otherKey)).toThrow();
  });

  it("rejects a key that isn't 32 bytes", () => {
    const shortKey = Buffer.alloc(16).toString("base64");
    expect(() => encryptWithKey("x", shortKey)).toThrow(/32 bytes/);
    expect(() => decryptWithKey(encryptWithKey("x", key), shortKey)).toThrow(/32 bytes/);
  });

  it("rejects malformed data", () => {
    expect(() => decryptWithKey("v2:abc", key)).toThrow(/format/);
    expect(() => decryptWithKey("nocolon", key)).toThrow(/format/);
  });

  it("still decrypts tokens saved in the old AES-CBC format", () => {
    const legacy = encryptLegacyCbc("1//old-refresh-token", key);

    expect(isLegacyCiphertext(legacy)).toBe(true);
    expect(decryptWithKey(legacy, key)).toBe("1//old-refresh-token");
  });
});
