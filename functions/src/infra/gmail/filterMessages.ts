import { logger } from "firebase-functions";
import { gmail_v1 } from "googleapis";

/**
 * ソート済みメッセージリストから、lastMsgId より新しいメッセージを抽出して返す。
 *
 * @param sortedList 新しい順にソートされた [id, message] のペア配列
 * @param lastMsgId  直近の処理済みメッセージID（null なら全件対象）
 * @returns filteredMessages is empty when there is no new mail.
 *          mostRecentMsgId is the newest new mail, or null when there is none.
 */
export function filterMessages(
  sortedList: [string, gmail_v1.Schema$Message][],
  lastMsgId?: string | null
): {
  filteredMessages: Record<string, gmail_v1.Schema$Message>;
  mostRecentMsgId: string | null;
} {
  const filteredMessages: Record<string, gmail_v1.Schema$Message> = {};
  let mostRecentMsgId: string | null = null;

  for (const [id, message] of sortedList) {
    // lastMsgId に達したら、それ以降（古い）は無視
    if (lastMsgId != null && id === lastMsgId) {
      logger.info(`Found lastMsgId again. ${id}`);
      break;
    }

    // 最初の1件目（最新）を記録
    if (!mostRecentMsgId) {
      mostRecentMsgId = id;
    }

    // message が null/undefined の場合はスキップ
    if (!message) {
      logger.warn(`Message is null or undefined for ID: ${id}`);
      continue;
    }

    filteredMessages[id] = message;
  }

  return { filteredMessages, mostRecentMsgId };
}
