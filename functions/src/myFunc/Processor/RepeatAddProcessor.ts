// RepeatAddProcessor.ts (または適切なファイル名)
import { GeneratedType } from "../../constants/GeneratedType";
import { RepeatFrequency } from "../../constants/RepeatFrequency";
import {
  FuncResult,
  FuncResultWithData,
  FuncStatus,
} from "../../type/FuncStatus";
import { RepeatAdd } from "../../type/RepeatAdd";
import { ExpenseService } from "../FirestoreService/ExpenseService";
import { RepeatAddService } from "../FirestoreService/RepeatAddService";
import {
  getEverydayOfMonth,
  getSingleDayOfMonth,
  getSpecificWeekdaysOfMonth,
  getWeekdaysOfMonth,
  getWeekendsOfMonth,
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
    month: number /* 1~12 */,
    filter_datetime: Date | null = null /* フィルター用のdatetime。nullならフィルターなし */
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
    console.log(`This is {freq}`);
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
        datesArr = getSpecificWeekdaysOfMonth(year, month, daysOfWeek);
        break;

      case RepeatFrequency.WEEKENDS:
        datesArr = getWeekendsOfMonth(year, month);
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
        /* repeatAddの月が引数の月が一致していたら今月追加 */
        const _month = repeatAdd.frequencyInfo.month;
        const _dayOfYear = repeatAdd.frequencyInfo.day;
        if (_month == null || _dayOfYear == null) {
          return {
            status: FuncStatus.ERROR,
            message: `freqでmonthまたはdayが設定されていません。month:${_month} day:${_dayOfYear}`,
          };
        }
        if (_month === month) {
          /* ここまで来て初めて日付を取得できる */
          const _dateOfYear = getSingleDayOfMonth(year, month, _dayOfYear);
          if (_dateOfYear == null) {
            return {
              status: FuncStatus.ERROR,
              message: `${year}/${month}/${_dayOfYear}は存在しません`,
            };
          }
          datesArr = [_dateOfYear];
        } else {
          /* 月が違うときは追加しないから空 */
          datesArr = [];
        }
        break;

      default:
        return {
          status: FuncStatus.ERROR,
          message: `RepeatAdd[${freq}]は対応していません。`,
        };
    }

    const retDates = setTimeToDates(datesArr, hour, minute);
    if (filter_datetime != null) {
      /* filter_datetimeが指定されている場合は、filter_datetime以降のものだけを返す */
      const filteredDates = retDates.filter((date) => date >= filter_datetime);
      return {
        status: FuncStatus.SUCCESS,
        data: filteredDates,
      };
    }

    return {
      status: FuncStatus.SUCCESS,
      data: retDates,
    };
  }

  async addExpenseFromRepeatAdd(
    userId: string,
    repeatAdd: RepeatAdd,
    datetime: Date
  ): Promise<FuncResult> {
    const expense = repeatAdd.expense;
    if (expense == null) {
      return {
        status: FuncStatus.ERROR,
        message: `Expense is null for repeat add ${repeatAdd.id}.`,
      };
    }

    /* 必要なものを加えていかないと、、 */
    expense.generatedType = `${GeneratedType.REPEAT_ADD}_${repeatAdd.id}`;
    expense.datetime = datetime.toISOString();
    expense.timestamp = Date.now();

    const addExpenseStatus = await this.expenseService.addExpenseWithId(
      userId,
      expense
    );
    if (addExpenseStatus.status !== FuncStatus.SUCCESS) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to add expense for repeat add ${repeatAdd.id}: ${addExpenseStatus.message}`,
      };
    }

    /* ここまできたら成功 */
    return {
      status: FuncStatus.SUCCESS,
      message: `Expense added successfully for repeat add ${repeatAdd.id}.`,
    };
  }

  /**
   * あるユーザーのrepeatAddをすべて取得して、
   * 各repeatAddからexpenseを追加していく
   */
  async addExpensesFromAllRepeatAdd(userId: string): Promise<FuncResult> {
    console.log("Processing user ID:", userId);
    /* まずはユーザーのRepeatAddをすべて取ってくる */
    const repeatAddsStatus = await this.repeatAddService.getAllRepeatAdds(
      userId
    );
    if (repeatAddsStatus.status !== FuncStatus.SUCCESS) {
      /* 失敗したらエラーを出して終了 */
      return {
        status: FuncStatus.ERROR,
        message: `Failed to retrieve repeat adds for user ${userId}: ${repeatAddsStatus.message}`,
      };
    }
    const repeatAdds = repeatAddsStatus.data;
    if (repeatAdds == null) {
      return {
        status: FuncStatus.ERROR,
        message: `No repeat adds found for user ${userId}.`,
      };
    }

    /* 次で使うので現在の年と月を取得 */
    const DateTime = require("luxon").DateTime; //このように書かないとimportできないっぽい。
    const tokyonow = DateTime.now().setZone("Asia/Tokyo");
    const currentYear = tokyonow.getFullYear();
    const currentMonth = tokyonow.getMonth() + 1; // 月は0から始まるので+1
    /* 取得したrepeatAddを回して */

    let addedExpenseCount = 0;
    for (const repeatAdd of Object.values(repeatAdds)) {
      addedExpenseCount = 0; // 初期化
      const targetResult = this.getTargetDateFromRepeatAdd(
        repeatAdd,
        currentYear,
        currentMonth
      );
      if (targetResult.status !== FuncStatus.SUCCESS) {
        console.error(
          `Failed to get target dates for repeat add ${repeatAdd.id}: ${targetResult.message}`
        );
        continue;
      }
      const targetDates = targetResult.data;
      if (targetDates == null) {
        console.error(`No target dates found for repeat add ${repeatAdd.id}.`);
        continue;
      }

      /* targetDatesをループして、expenseに加えていく */
      for (const targetDate of targetDates) {
        const addExpenseStatus = await this.addExpenseFromRepeatAdd(
          userId,
          repeatAdd,
          targetDate
        );
        if (addExpenseStatus.status !== FuncStatus.SUCCESS) {
          console.error(
            `Failed to add expense for repeat add ${
              repeatAdd.id
            } on ${targetDate.toISOString()}: ${addExpenseStatus.message}`
          );
        } else {
          addedExpenseCount++;
        }
      }

      if (addedExpenseCount !== targetDates.length) {
        console.warn(
          `Not all expenses were added for repeat add ${repeatAdd.id}. Added: ${addedExpenseCount}, Target: ${targetDates.length}`
        );
      }
    }
    return {
      status: FuncStatus.SUCCESS,
      message: `Processed repeat adds for user ${userId}.`,
    };
  }
}
