import { gmail_v1 } from "googleapis";

export function extractHtmlBody(
  payload: gmail_v1.Schema$MessagePart | undefined,
  stripHtml: boolean = false
): string | null {
  if (!payload) return null; // ✅ null なら早期リターン

  const findHtmlPart = (part: gmail_v1.Schema$MessagePart): string | null => {
    // 直接 text/html が見つかったら即返す
    if (part.mimeType === "text/html" && part.body?.data) {
      const html = Buffer.from(part.body.data, "base64url").toString("utf8");
      return stripHtml ? stripHtmlTags(html) : html;
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

    // // fallback: text/html
    // if (part.mimeType === "text/html" && part.body?.data) {
    //   const html = Buffer.from(part.body.data, "base64url").toString("utf8");
    //   return convert(html, { wordwrap: false }); // fallback: html→text
    // }

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

export function getSubjectFromMessage(
  message: gmail_v1.Schema$Message
): string | undefined | null {
  const headers = message.payload?.headers;
  if (!headers) return undefined;

  const subjectHeader = headers.find(
    (h) => h.name?.toLowerCase() === "subject"
  );
  return subjectHeader?.value;
}

/**
 * HTMLタグを削除してテキストのみを抽出する
 * @param html HTML文字列
 * @returns HTMLタグが削除されたテキスト
 */
export function stripHtmlTags(html: string): string {
  if (!html) return "";

  let text = html;

  // style と script タグとその内容を削除
  text = text.replace(/<style[^>]*>[\s\S]*?<\/style>/gi, "");
  text = text.replace(/<script[^>]*>[\s\S]*?<\/script>/gi, "");

  // ブロック要素の前に改行を追加（div, p, h1-h6, li, tr, td, th など）
  text = text.replace(/<\/(div|p|h[1-6]|li|tr|td|th|section|article|header|footer|main|aside|nav|form)>/gi, "\n");

  // br タグを改行に変換
  text = text.replace(/<br\s*\/?>/gi, "\n");

  // 他のブロック要素の開始タグの前に改行を追加（ただしstyle/scriptは既に削除済み）
  text = text.replace(/<(div|p|h[1-6]|li|tr|td|th|section|article|header|footer|main|aside|nav|form)[^>]*>/gi, "\n");

  // HTMLタグを削除
  text = text.replace(/<[^>]*>/g, "");

  // HTMLエンティティをデコード（基本的なもの）
  const entityMap: { [key: string]: string } = {
    "&nbsp;": " ",
    "&amp;": "&",
    "&lt;": "<",
    "&gt;": ">",
    "&quot;": '"',
    "&#39;": "'",
    "&nbsp": " ",
  };

  for (const [entity, char] of Object.entries(entityMap)) {
    text = text.replace(new RegExp(entity, "gi"), char);
  }

  // 改行と空白を適切に正規化
  text = text
    .replace(/[ \t]+/g, " ") // 連続する空白やタブを1つのスペースに
    .replace(/\n[ \t]+/g, "\n") // 改行後の空白を削除
    .replace(/[ \t]+\n/g, "\n") // 改行前の空白を削除
    .replace(/\n{3,}/g, "\n\n") // 3つ以上の連続する改行を2つに
    .trim();

  return text;
}