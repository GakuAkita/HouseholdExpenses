// RepeatAddProcessor.ts (または適切なファイル名)
import { convertDayNamesToNums } from "../../constants/DayOfWeek";
import { RepeatFrequency } from "../../constants/RepeatFrequency";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { RepeatAdd } from "../../type/RepeatAdd";
import { ExpenseService } from "../FirestoreService/ExpenseService";
import { RepeatAddService } from "../FirestoreService/RepeatAddService";
import {
  getEverydayOfMonth,
  getSingleDayOfMonth,
  getSpecificWeekdaysOfMonth,
  getWeekdaysOfMonth,
  setTimeToDates,
} from "../utility/getDays";

export class RepeatAddProcessor {
  constructor(
    private repeatAddService: RepeatAddService,
    private expenseService: ExpenseService
  ) {}

  /**
   * repeatAddを引数にして、そこから日付と時間を抽出する。
   * その後、getDaysの関数でいれる日付を取得する
   */
  getTargetDateFromRepeatAdd(
    repeatAdd: RepeatAdd,
    year: number,
    month: number /* 1~12 */
  ): FuncResultWithData<Date[]> {
    /* 時間だけは共通なので、時間を取得しておく */
    const hour = repeatAdd.frequencyInfo.hour;
    const minute = repeatAdd.frequencyInfo.minute;
    if (hour == null || minute == null) {
      return {
        status: FuncStatus.ERROR,
        message: `hourかminuteが設定されていません。hour:${hour} minute:${minute}`,
      };
    }
    const freq = repeatAdd.frequencyInfo.frequency;

    let datesArr: Date[] = [];
    switch (freq) {
      case RepeatFrequency.EVERYDAY:
        datesArr = getEverydayOfMonth(year, month);
        break;

      case RepeatFrequency.EVERY_WEEK:
        const daysOfWeek = repeatAdd.frequencyInfo.dayOfWeek;
        /* 保存されているのは文字列なので、数値に変換する */
        console.log(daysOfWeek);
        if (daysOfWeek == null) {
          return {
            status: FuncStatus.ERROR,
            message: `曜日が保存されていません repeatAdd:${repeatAdd.id}`,
          };
        }

        const daysOfWeekNums = convertDayNamesToNums(daysOfWeek);
        datesArr = getSpecificWeekdaysOfMonth(year, month, daysOfWeekNums);
        break;

      case RepeatFrequency.WEEKENDS:
        datesArr = getWeekdaysOfMonth(year, month);
        break;

      case RepeatFrequency.WEEKDAYS:
        datesArr = getWeekdaysOfMonth(year, month);
        break;

      case RepeatFrequency.EVERY_MONTH:
        const _day = repeatAdd.frequencyInfo.day;
        if (_day == null) {
          return {
            status: FuncStatus.ERROR,
            message: "freqでdayが設定されていません",
          };
        }
        const _date = getSingleDayOfMonth(year, month, _day);
        if (_date == null) {
          /* 日付がnullだったら、存在しないってこと。 */
          return {
            status: FuncStatus.ERROR,
            message: `${year}/${month}/${_day}は存在しません`,
          };
        }
        datesArr = [_date];
        break;

      case RepeatFrequency.EVERY_YEAR:
        /*  */
        break;

      default:
        return {
          status: FuncStatus.ERROR,
          message: `RepeatAdd[${freq}]は対応していません。`,
        };
    }

    const retDates = setTimeToDates(datesArr, hour, minute);
    return {
      status: FuncStatus.SUCCESS,
      data: retDates,
    };
  }
}
