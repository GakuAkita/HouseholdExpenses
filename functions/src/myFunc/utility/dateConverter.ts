import { logger } from "firebase-functions";
import { TimeZone } from "../../constants/TimeZone";

/**
 * 任意のタイムゾーンでのDateを、UTCのISO文字列に変換
 * @param date - 対象の Date インスタンス
 * @param timeZone - 例: 'Asia/Tokyo', 'America/New_York'
 * @returns UTCのISO文字列（例: '2024-06-01T05:00:00.000Z'）
 */
const { DateTime } = require("luxon");
export function convertToUtcIsoString(date: Date, timeZone: string): string {
  const dt = DateTime.fromJSDate(date, { zone: timeZone });

  if (!dt.isValid) {
    logger.error(`Invalid time zone: ${timeZone} — ${dt.invalidExplanation}`);
    timeZone = TimeZone.JST; //事故っていたら日本時間に設定
  }

  return dt.toUTC().toISO();
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
