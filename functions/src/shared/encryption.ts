import crypto from "crypto";

/*
 * Encrypts refresh tokens with AES-256-GCM, so a changed ciphertext fails to decrypt
 * instead of decrypting to something else.
 *
 * Format: "v2:<iv hex>:<auth tag hex>:<ciphertext hex>"
 *
 * Tokens saved before v2 are AES-256-CBC: "<iv hex>:<ciphertext hex>". They still decrypt,
 * and isLegacyCiphertext() tells callers to save them again in the new format.
 */

const VERSION = "v2";
const GCM_IV_BYTES = 12;
const GCM_TAG_BYTES = 16;

const keyFrom = (base64Key: string): Buffer => {
  const key = Buffer.from(base64Key, "base64");
  if (key.length !== 32) {
    throw new Error(`The encryption key must be 32 bytes (base64). It is ${key.length} bytes.`);
  }
  return key;
};

export function encryptWithKey(plainText: string, base64Key: string): string {
  const key = keyFrom(base64Key);
  const iv = crypto.randomBytes(GCM_IV_BYTES);

  const cipher = crypto.createCipheriv("aes-256-gcm", key, iv, { authTagLength: GCM_TAG_BYTES });
  const encrypted = Buffer.concat([cipher.update(plainText, "utf8"), cipher.final()]);
  const tag = cipher.getAuthTag();

  return [VERSION, iv.toString("hex"), tag.toString("hex"), encrypted.toString("hex")].join(":");
}

/** true for a token saved in the old AES-CBC format. */
export function isLegacyCiphertext(encryptedData: string): boolean {
  return !encryptedData.startsWith(`${VERSION}:`);
}

export function decryptWithKey(encryptedData: string, base64Key: string): string {
  const key = keyFrom(base64Key);
  return isLegacyCiphertext(encryptedData)
    ? decryptLegacyCbc(encryptedData, key)
    : decryptGcm(encryptedData, key);
}

const decryptGcm = (encryptedData: string, key: Buffer): string => {
  const [, ivHex, tagHex, encryptedHex, ...rest] = encryptedData.split(":");
  if (!ivHex || !tagHex || encryptedHex === undefined || rest.length > 0) {
    throw new Error("Invalid encrypted data format");
  }
  const iv = Buffer.from(ivHex, "hex");
  const tag = Buffer.from(tagHex, "hex");
  if (iv.length !== GCM_IV_BYTES || tag.length !== GCM_TAG_BYTES) {
    throw new Error("Invalid encrypted data format");
  }

  const decipher = crypto.createDecipheriv("aes-256-gcm", key, iv, { authTagLength: GCM_TAG_BYTES });
  decipher.setAuthTag(tag);
  /* final() throws when the ciphertext, IV or tag was changed, or the key is wrong. */
  return Buffer.concat([decipher.update(Buffer.from(encryptedHex, "hex")), decipher.final()]).toString("utf8");
};

/** The format used before v2. Only for reading tokens saved back then. */
const decryptLegacyCbc = (encryptedData: string, key: Buffer): string => {
  const [ivHex, encryptedHex, ...rest] = encryptedData.split(":");
  if (!ivHex || !encryptedHex || rest.length > 0) throw new Error("Invalid encrypted data format");

  const decipher = crypto.createDecipheriv("aes-256-cbc", key, Buffer.from(ivHex, "hex"));
  return Buffer.concat([decipher.update(Buffer.from(encryptedHex, "hex")), decipher.final()]).toString("utf8");
};
