import { gmail_v1 } from "googleapis";
import { FuncResultWithData, FuncStatus } from "../../../type/FuncStatus";
import { GmailApiClient } from "../../Client/GmailApiClient";
import { getInternalDateMillisFromMessage } from "./getInternalDate";

/**
 * MsgIdの配列を引数に渡して、
 * Detailを取得し順番を並び替える
 */

/**
 * MsgIdの配列を引数に渡して、
 * Detailを取得し順番を並び替える
 */
export async function getMessageDetailsSortedList(
  gmailClient: GmailApiClient,
  msgIdList: string[]
): Promise<FuncResultWithData<[string, gmail_v1.Schema$Message][]>> {
  const messageMap: Record<string, gmail_v1.Schema$Message> = {};

  for (const id of msgIdList) {
    const res = await gmailClient.getMessageDetail(id);
    if (res.status !== FuncStatus.SUCCESS || !res.data) {
      return {
        status: FuncStatus.ERROR,
        message: `getMessageDetail failed: id=${id} msg=${res.message}`,
      };
    }
    messageMap[id] = res.data;
  }

  /**
   * internalDate順(新しい順)に並び替え
   */
  const sortedEntries = Object.entries(messageMap).sort((a, b) => {
    const dateA = getInternalDateMillisFromMessage(a[1]);
    const dateB = getInternalDateMillisFromMessage(b[1]);

    // null 安全性を確保：null は最も古いとみなす（＝最後に来る）
    if (dateA === null && dateB === null) return 0;
    if (dateA === null) return 1;
    if (dateB === null) return -1;

    return dateB - dateA; // 降順（新しい順）
  });

  return {
    status: FuncStatus.SUCCESS,
    data: sortedEntries,
  };
}
