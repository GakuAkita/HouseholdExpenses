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
    console.error(`Invalid time zone: ${timeZone} — ${dt.invalidExplanation}`);
    return TimeZone.JST; //事故っていたら日本時間に設定
  }

  return dt.toUTC().toISO();
}
