import { logger } from "firebase-functions";
import { gmail_v1 } from "googleapis";
import { FuncResultWithData, FuncStatus } from "../../../type/FuncStatus";

/**
 * ソート済みメッセージリストから、
 * lastMsgId より新しいメッセージを抽出して返す。
 *
 * @param sortedList 新しい順にソートされた [id, message] のペア配列
 * @param lastMsgId  直近の処理済みメッセージID（null なら全件対象）
 * @returns FuncResultWithData<{ filteredMessages, mostRecentMsgId }>
 */
export function filterMessages(
  sortedList: [string, gmail_v1.Schema$Message][],
  lastMsgId?: string | null
): FuncResultWithData<{
  filteredMessages: Record<string, gmail_v1.Schema$Message>;
  mostRecentMsgId: string | null;
}> {
  try {
    const filteredMessages: Record<string, gmail_v1.Schema$Message> = {};
    let mostRecentMsgId: string | null = null;

    for (const [id, message] of sortedList) {
      // lastMsgId に達したら、それ以降（古い）は無視
      if (lastMsgId != null && id === lastMsgId) {
        /**
         * lastMsgIdがundefinedの場合でも
         * lastMsgId!=nullはtrueが返る
         */
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

    // 結果が空の場合
    if (Object.keys(filteredMessages).length === 0) {
      return {
        status: FuncStatus.EMPTY,
        message: "No new messages found.",
        data: { filteredMessages, mostRecentMsgId },
      };
    }

    return {
      status: FuncStatus.SUCCESS,
      message: "Messages filtered successfully.",
      data: { filteredMessages, mostRecentMsgId },
    };
  } catch (error) {
    logger.error("Error filtering messages", error);
    return {
      status: FuncStatus.ERROR,
      message:
        (error as Error).message ?? "Unknown error while filtering messages.",
    };
  }
}
