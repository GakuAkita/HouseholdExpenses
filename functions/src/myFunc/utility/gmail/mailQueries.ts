import { logger } from "firebase-functions";
import { FuncResultWithData, FuncStatus } from "../../../type/FuncStatus";
import { GmailApiClient } from "../../Client/GmailApiClient";

export async function getRakutenPayMailIds(
  gmailClient: GmailApiClient,
  startTime: number /* 時間で絞るための開始時刻(秒:整数) */,
  endTime: number /* 時間で絞るための終了時刻(秒:整数) */
): Promise<FuncResultWithData<string[]>> {
  /* まずはクエリをして楽天Payを抽出する */
  const subjectIncluded = "楽天ペイアプリご利用内容確認メール";
  const mailFrom = "no-reply@pay.rakuten.co.jp";

  /**
   * gmailのクエリは秒数+1~秒数-1でクエリがかかるらしい。
   * したがって、endTimeに+1をしてendTimeも含めるようにする
   * ちょっとここらへんが怖いな、
   */
  const endTimeAdded = endTime + 1;
  const query = `subject:${subjectIncluded} from:${mailFrom} after:${startTime} before:${endTimeAdded}`;
  logger.debug(`Query:${query}`);
  const funcResult = await gmailClient.queryMessages(query);
  return funcResult;
}

export async function getAmazonKindleMailIds(
  gmailClient: GmailApiClient,
  startTime: number,
  endTime: number
): Promise<FuncResultWithData<string[]>> {
  const mailFrom = "digital-no-reply@amazon.co.jp";
  const wordIncluded =
    "Kindle"; /* まあこれなくてもいいけど、、一応つけておく。本文または件名に含まれる */

  const endTimeAdded = endTime + 1;
  const query = `from:${mailFrom} ${wordIncluded} after:${startTime} before:${endTimeAdded}`;
  logger.debug(`Query:${query}`);
  const funcResult = await gmailClient.queryMessages(query);
  return funcResult;
}

export async function getAmazonSubscribeNewRegsterMailIds(
  gmailClient: GmailApiClient,
  startTime: number,
  endTime: number
): Promise<FuncResultWithData<string[]>> {
  const mailFrom = "no-reply@amazon.co.jp";
  const subject = "新しい定期おトク便のご登録";
  const endTimeAdded = endTime + 1;
  const query = `from:${mailFrom} subject:${subject} after:${startTime} before:${endTimeAdded}`;
  return {
    status: FuncStatus.ERROR,
  };
}

export async function getAmazonSubscribeCancelRegisterMailIds(
  gmailClient: GmailApiClient,
  startTime: number,
  endTime: number
): Promise<FuncResultWithData<string[]>> {
  const mailFrom = "no-reply@amazon.co.jp";
  const subject = "定期購入はキャンセルされました";
  const endTimeAdded = endTime + 1;
  const query = `from:${mailFrom} subject:${subject} after:${startTime} before:${endTimeAdded}`;
  return {
    status: FuncStatus.ERROR,
  };
}

export async function getAmazonNextShipNotifyMailIds(
  gmailClient: GmailApiClient,
  startTime: number,
  endTime: number
): Promise<FuncResultWithData<string[]>> {
  const mailFrom = "no-reply@amazon.co.jp";
  const subject = "次回の配送を確認する";
  const endTimeAdded = endTime + 1;
  const query = `from:${mailFrom} subject:${subject} after:${startTime} before:${endTimeAdded}`;
  logger.log(`Query:${query}`);
  const funcResult = await gmailClient.queryMessages(query);
  return funcResult;
}

export async function getAmazonCurrentlyShippedMailIds(
  gmailClient: GmailApiClient,
  startTime: number,
  endTime: number
): Promise<FuncResultWithData<string[]>> {
  const mailFrom = "shipment-tracking@amazon.co.jp";
  const subject = "配達中:";
  const endTimeAdded = endTime + 1;
  const query = `from:${mailFrom} subject:${subject} after:${startTime} before:${endTimeAdded}`;
  return {
    status: FuncStatus.ERROR,
  };
}

export async function getShikokuElectricMailIds(
  gmailClient: GmailApiClient,
  startTime: number,
  endTime: number
): Promise<FuncResultWithData<string[]>> {
  const mailFrom = "yonden-con@yonden.co.jp";
  const wordIncluded = "【四国電力】電気料金等のお知らせ";
  const endTimeAdded = endTime + 1;
  const query = `from:${mailFrom} ${wordIncluded} after:${startTime} before:${endTimeAdded}`;
  logger.debug(`Query:${query}`);
  const funcResult = await gmailClient.queryMessages(query);
  return funcResult;
}

export async function getAmazonItemMailIds(
  gmailClient: GmailApiClient,
  startTime: number,
  endTime: number
): Promise<FuncResultWithData<string[]>> {
  const mailFrom = "auto-confirm@amazon.co.jp";
  const endTimeAdded = endTime + 1;
  const query = `from:${mailFrom} after:${startTime} before:${endTimeAdded}`;
  logger.debug(`Query:${query}`);
  const funcResult = await gmailClient.queryMessages(query);
  return funcResult;
}

export async function getUdemyMailIds(
  gmailClient: GmailApiClient,
  startTime: number,
  endTime: number
): Promise<FuncResultWithData<string[]>> {
  const mailFrom = "hello@alerts.udemy.com";
  const endTimeAdded = endTime + 1;
  const query = `from:${mailFrom} after:${startTime} before:${endTimeAdded}`;
  logger.debug(`Query:${query}`);
  const funcResult = await gmailClient.queryMessages(query);
  return funcResult;
}

export async function getRakutenCardETCMailIds(
  gmailClient: GmailApiClient,
  startTime: number,
  endTime: number
): Promise<FuncResultWithData<string[]>> {
  const mailFrom = "info@mail.rakuten-card.co.jp";
  const endTimeAdded = endTime + 1;
  const query = `from:${mailFrom} ETCカード売上 after:${startTime} before:${endTimeAdded}`;
  logger.debug(`Query:${query}`);
  const funcResult = await gmailClient.queryMessages(query);
  return funcResult;
}
