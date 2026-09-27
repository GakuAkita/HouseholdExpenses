import { logger } from "firebase-functions";
import { AmazonMailSubjects } from "../../type/AmazonMailSubjects";
import { GmailClient } from "./GmailApiClient";

/*
 * Gmail queries for each mail type. startTime and endTime are UNIX seconds.
 * Gmailのクエリは秒数+1~秒数-1でクエリがかかるらしい。したがって、endTimeに+1をしてendTimeも含めるようにする。
 */

export async function getRakutenPayMailIds(
  gmailClient: GmailClient,
  startTime: number,
  endTime: number
): Promise<string[]> {
  const subjects = ["楽天ペイアプリご利用内容確認メール", "楽天ペイお支払い完了のお知らせ"];
  const mailFrom = "no-reply@pay.rakuten.co.jp";
  const query =
    `subject:{${subjects.join(" ")}} ` +
    `from:${mailFrom} ` +
    `after:${startTime} ` +
    `before:${endTime + 1}`;
  logger.debug(`Query:${query}`);
  return gmailClient.queryMessages(query);
}

export async function getAmazonKindleMailIds(
  gmailClient: GmailClient,
  startTime: number,
  endTime: number
): Promise<string[]> {
  const mailFrom = "digital-no-reply@amazon.co.jp";
  /* まあこれなくてもいいけど、、一応つけておく。本文または件名に含まれる */
  const wordIncluded = "Kindle";
  const query = `from:${mailFrom} ${wordIncluded} -subject:予約注文 after:${startTime} before:${endTime + 1}`;
  return gmailClient.queryMessages(query);
}

/** 次回配送・価格変更・在庫切れ・キャンセルの定期便メール */
export async function getAmazonSubscribeNextShipNotifyAndCancelMailIds(
  gmailClient: GmailClient,
  startTime: number,
  endTime: number,
  maxResult: number = 10
): Promise<string[]> {
  const mailFrom = "no-reply@amazon.co.jp";
  const subjects = [
    AmazonMailSubjects.NEXT_SHIPMENT,
    AmazonMailSubjects.PRICE_CHANGED,
    AmazonMailSubjects.ITEM_RUNOUT,
    AmazonMailSubjects.CANCELED_SUBSCRIPTION,
  ];
  const subjectQuery = subjects.map((s) => `"${s}"`).join(" OR ");
  const query = `from:${mailFrom} subject:${subjectQuery} after:${startTime} before:${endTime + 1}`;
  logger.log(`Query:${query}`);
  return gmailClient.queryMessages(query, maxResult);
}

export async function getAmazonDispatchedMailIds(
  gmailClient: GmailClient,
  startTime: number,
  endTime: number,
  maxResult: number = 5
): Promise<string[]> {
  const mailFrom = "shipment-tracking@amazon.co.jp";
  const subject = "発送済み";
  const query = `from:${mailFrom} subject:${subject} after:${startTime} before:${endTime + 1}`;
  return gmailClient.queryMessages(query, maxResult);
}

export async function getShikokuElectricMailIds(
  gmailClient: GmailClient,
  startTime: number,
  endTime: number
): Promise<string[]> {
  const mailFrom = "yonden-con@yonden.co.jp";
  const wordIncluded = "【四国電力】電気料金等のお知らせ";
  const query = `from:${mailFrom} ${wordIncluded} after:${startTime} before:${endTime + 1}`;
  return gmailClient.queryMessages(query);
}

export async function getAmazonItemMailIds(
  gmailClient: GmailClient,
  startTime: number,
  endTime: number
): Promise<string[]> {
  const mailFrom = "auto-confirm@amazon.co.jp";
  const query = `from:${mailFrom} after:${startTime} before:${endTime + 1}`;
  return gmailClient.queryMessages(query);
}

export async function getUdemyMailIds(
  gmailClient: GmailClient,
  startTime: number,
  endTime: number
): Promise<string[]> {
  const mailFrom = "hello@alerts.udemy.com";
  const query = `from:${mailFrom} after:${startTime} before:${endTime + 1}`;
  return gmailClient.queryMessages(query);
}

export async function getRakutenCardETCMailIds(
  gmailClient: GmailClient,
  startTime: number,
  endTime: number
): Promise<string[]> {
  const mailFrom = "info@mail.rakuten-card.co.jp";
  const query = `from:${mailFrom} ETCカード売上 after:${startTime} before:${endTime + 1}`;
  return gmailClient.queryMessages(query);
}
