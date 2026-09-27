import { logger } from "firebase-functions";
import { RepeatFrequency } from "../constants/RepeatFrequency";
import { TimeZone } from "../constants/TimeZone";
import { Frequency } from "../type/RepeatAdd";
import {
  getEverydayOfMonth,
  getSingleDayOfMonth,
  getSpecificWeekdaysOfMonth,
  getWeekdaysOfMonth,
  getWeekendsOfMonth,
  setTimeToDates,
} from "./getDays";

/**
 * The dates in the month when a RepeatAdd adds an expense, at the RepeatAdd's time in the user's time zone.
 * Throws when the frequency is incomplete or unknown, or the day doesn't exist in the month.
 */
export function getRepeatAddTargetDates(
  frequencyInfo: Frequency,
  year: number,
  month: number /* 1~12 */,
  filter_datetime: Date | null = null /* フィルター用のdatetime。nullならフィルターなし */,
  userTimeZone: string = TimeZone.JST,
  repeatAddId?: string
): Date[] {
  /* 時間だけは共通なので、時間を取得しておく */
  const hour = frequencyInfo.hour;
  const minute = frequencyInfo.minute;
  if (hour == null || minute == null) {
    throw new Error(`hourかminuteが設定されていません。hour:${hour} minute:${minute}`);
  }
  const freq = frequencyInfo.frequency;

  let datesArr: Date[] = [];
  logger.log(`This is ${freq}`);
  switch (freq) {
    case RepeatFrequency.EVERYDAY:
      datesArr = getEverydayOfMonth(year, month);
      break;

    case RepeatFrequency.EVERY_WEEK:
      const daysOfWeek = frequencyInfo.dayOfWeek;
      /* 保存されているのは文字列なので、数値に変換する */
      logger.log(daysOfWeek);
      if (daysOfWeek == null) {
        throw new Error(`曜日が保存されていません repeatAdd:${repeatAddId}`);
      }
      datesArr = getSpecificWeekdaysOfMonth(year, month, daysOfWeek);
      break;

    case RepeatFrequency.WEEKENDS:
      datesArr = getWeekendsOfMonth(year, month);
      break;

    case RepeatFrequency.WEEKDAYS:
      datesArr = getWeekdaysOfMonth(year, month);
      break;

    case RepeatFrequency.EVERY_MONTH:
      const _day = frequencyInfo.day;
      if (_day == null) {
        throw new Error("freqでdayが設定されていません");
      }
      const _date = getSingleDayOfMonth(year, month, _day);
      if (_date == null) {
        /* 日付がnullだったら、存在しないってこと。 */
        throw new Error(`${year}/${month}/${_day}は存在しません`);
      }
      datesArr = [_date];
      break;

    case RepeatFrequency.EVERY_YEAR:
      /* repeatAddの月が引数の月が一致していたら今月追加 */
      const _month = frequencyInfo.month;
      const _dayOfYear = frequencyInfo.day;
      if (_month == null || _dayOfYear == null) {
        throw new Error(`freqでmonthまたはdayが設定されていません。month:${_month} day:${_dayOfYear}`);
      }
      if (_month === month) {
        /* ここまで来て初めて日付を取得できる */
        const _dateOfYear = getSingleDayOfMonth(year, month, _dayOfYear);
        if (_dateOfYear == null) {
          throw new Error(`${year}/${month}/${_dayOfYear}は存在しません`);
        }
        datesArr = [_dateOfYear];
      } else {
        /* 月が違うときは追加しないから空 */
        datesArr = [];
      }
      break;

    default:
      throw new Error(`RepeatAdd[${freq}]は対応していません。`);
  }

  logger.log(`datesArr: ${datesArr}`);

  const retDates = setTimeToDates(datesArr, hour, minute, userTimeZone);
  if (filter_datetime != null) {
    /* filter_datetimeが指定されている場合は、filter_datetime以降のものだけを返す */
    const filteredDates = retDates.filter((date) => date >= filter_datetime);
    return filteredDates;
  }

  /**
   * yyyy年-mm月-dd日HH:MM:00(UTC)になっている。
   * HHとMMはrepeatAddで指定された時間。
   */
  logger.log(`target dates: ${retDates}`);

  return retDates;
}
