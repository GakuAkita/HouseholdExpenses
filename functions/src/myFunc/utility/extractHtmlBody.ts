export function extractHtmlBody(payload: any): string | null {
  const findHtmlPart = (part: any): string | null => {
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
