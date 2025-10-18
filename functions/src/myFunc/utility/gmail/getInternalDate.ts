import { gmail_v1 } from "googleapis";

/**
 * GmailメッセージからinternalDateをUNIX秒（number）で取得するユーティリティ関数
 * @param message gmail_v1.Schema$Message
 * @returns internalDate（ミリ秒）または null
 */
export function getInternalDateMillisFromMessage(
  message: gmail_v1.Schema$Message
): number | null {
  if (!message || !message.internalDate) return null;

  const millis = parseInt(message.internalDate, 10);
  return isNaN(millis) ? null : millis;
}

/**
 * internalDate順(新しい順に並び替え)
 */
type SortOrder = "asc" | "desc";

/**
 * Sort messages by internalDate
 * @param messageMap - メッセージオブジェクトのマップ
 * @param order - "asc"なら古い順、"desc"なら新しい順（デフォルトは "desc"）
 */
export function sortGmailMessagesByDate(
  messageMap: Record<string, any>,
  order: SortOrder = "desc"
): [string, gmail_v1.Schema$Message][] {
  const sortedEntries = Object.entries(messageMap).sort((a, b) => {
    const dateA = getInternalDateMillisFromMessage(a[1]);
    const dateB = getInternalDateMillisFromMessage(b[1]);

    // null 安全性：null は最も古いとみなす
    if (dateA === null && dateB === null) return 0;
    if (dateA === null) return 1;
    if (dateB === null) return -1;

    // 降順なら dateB - dateA、昇順なら dateA - dateB
    return order === "desc" ? dateB - dateA : dateA - dateB;
  });

  return sortedEntries;
}
