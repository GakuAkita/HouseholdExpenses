import { gmail_v1 } from "googleapis";
import { GmailClient } from "./GmailApiClient";
import { sortGmailMessagesByDate } from "./getInternalDate";

/** Fetches the mails and returns them as [id, message], newest first. */
export async function getMessageDetailsSortedList(
  gmailClient: GmailClient,
  msgIdList: string[]
): Promise<[string, gmail_v1.Schema$Message][]> {
  const messageMap: Record<string, gmail_v1.Schema$Message> = {};
  for (const id of msgIdList) {
    messageMap[id] = await gmailClient.getMessageDetail(id);
  }
  return sortGmailMessagesByDate(messageMap, "desc");
}
