import { logger } from "firebase-functions";
import { DateTime } from "luxon";
import { TimeZone } from "../constants/TimeZone";

/**
 * 任意のタイムゾーンでのDateを、UTCのISO文字列に変換
 * @param date - 対象の Date インスタンス
 * @param timeZone - 例: 'Asia/Tokyo', 'America/New_York'
 * @returns UTCのISO文字列（例: '2024-06-01T05:00:00.000Z'）
 */
export function convertToUtcIsoString(date: Date, timeZone: string): string {
  let dt = DateTime.fromJSDate(date, { zone: timeZone });
  if (!dt.isValid) {
    logger.error(`Invalid time zone: ${timeZone} — ${dt.invalidExplanation}`);
    timeZone = TimeZone.JST; //事故っていたら日本時間に設定
    dt = DateTime.fromJSDate(date, { zone: TimeZone.JST }); // JSTで作り直す
  }

  const iso = dt.toUTC().toISO({ suppressMilliseconds: false, suppressSeconds: false });
  if (!iso) {
    throw new Error(`Invalid date: ${date}`);
  }
  return iso;
}

/**
 * "yyyy/mm/dd"の文字列をISO文字列に変換する
 */
export function convertyyyymmddToUTCIsoString(
  input: string,
  timeZone = TimeZone.JST
): string {
  const [yearStr, monthStr, dayStr] = input.split("/");
  const year = Number(yearStr);
  const month = Number(monthStr);
  const day = Number(dayStr);

  const date = new Date(year, month - 1, day);

  return convertToUtcIsoString(date, timeZone);
}
