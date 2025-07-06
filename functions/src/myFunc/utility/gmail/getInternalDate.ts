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
