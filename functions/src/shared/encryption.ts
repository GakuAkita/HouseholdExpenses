import crypto from "crypto";

export function encryptWithKey(plainText: string, base64Key: string): string {
  const key = Buffer.from(base64Key, "base64"); // 32 バイトに変換
  const iv = crypto.randomBytes(16);

  const cipher = crypto.createCipheriv("aes-256-cbc", key, iv);
  const encrypted = Buffer.concat([
    cipher.update(plainText, "utf8"),
    cipher.final(),
  ]);

  return iv.toString("hex") + ":" + encrypted.toString("hex");
}

export function decryptWithKey(
  encryptedData: string,
  base64Key: string
): string {
  const key = Buffer.from(base64Key, "base64");

  const [ivHex, encryptedHex] = encryptedData.split(":");
  if (!ivHex || !encryptedHex) throw new Error("Invalid encrypted data format");

  const iv = Buffer.from(ivHex, "hex");
  const encryptedText = Buffer.from(encryptedHex, "hex");

  const decipher = crypto.createDecipheriv("aes-256-cbc", key, iv);
  const decrypted = Buffer.concat([
    decipher.update(encryptedText),
    decipher.final(),
  ]);

  return decrypted.toString("utf8");
}
