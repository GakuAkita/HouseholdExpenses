import { gmail_v1 } from "googleapis";
import { convert } from "html-to-text";

export function extractHtmlBody(
  payload: gmail_v1.Schema$MessagePart | undefined
): string | null {
  if (!payload) return null; // ✅ null なら早期リターン

  const findHtmlPart = (part: gmail_v1.Schema$MessagePart): string | null => {
    // 直接 text/html が見つかったら即返す
    if (part.mimeType === "text/html" && part.body?.data) {
      return Buffer.from(part.body.data, "base64url").toString("utf8");
    }

    // multipart/alternative や multipart/mixed の処理
    if (part.mimeType?.startsWith("multipart/") && part.parts) {
      // 優先的に text/html を探す
      for (const subPart of part.parts) {
        const result = findHtmlPart(subPart);
        if (result) return result;
      }
    }

    return null;
  };
  return findHtmlPart(payload);
}

export function extractTextBody(
  payload: gmail_v1.Schema$MessagePart | undefined
): string | null {
  if (!payload) return null;

  const findPart = (part: gmail_v1.Schema$MessagePart): string | null => {
    // 優先: text/plain
    if (part.mimeType === "text/plain" && part.body?.data) {
      return Buffer.from(part.body.data, "base64url").toString("utf8");
    }

    // fallback: text/html
    if (part.mimeType === "text/html" && part.body?.data) {
      const html = Buffer.from(part.body.data, "base64url").toString("utf8");
      return convert(html, { wordwrap: false }); // fallback: html→text
    }

    // multipart系なら再帰的に探す
    if (part.mimeType?.startsWith("multipart/") && part.parts) {
      for (const subPart of part.parts) {
        const result = findPart(subPart);
        if (result) return result;
      }
    }

    return null;
  };

  return findPart(payload);
}
