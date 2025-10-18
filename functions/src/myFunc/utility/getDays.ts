import { WEEKDAYS, WEEKENDS } from "../../constants/DayOfWeek";

/**
 * 引数に曜日の配列を渡すと、
 * その月の曜日の日を全部返してくれる
 */
export function getSpecificWeekdaysOfMonth(
  year: number,
  month: number,
  targetDays: number[] /* 番号で渡す。DayOfWeekに対応 */
): Date[] {
  // month は 1〜12 として受け取る（Date は 0〜11 なので -1 する）
  const targetMonth = month - 1;
  const retDays: Date[] = [];

  const date = new Date(year, targetMonth, 1); /* 月の最初の日 */

  // 月の最初の日から月の最後の日までループ
  while (date.getMonth() === targetMonth) {
    const day = date.getDay(); // 0 = Sunday, 1 = Monday, ..., 6 = Saturday
    if (targetDays.includes(day)) {
      retDays.push(new Date(date));
    }
    date.setDate(date.getDate() + 1);
  }
  return retDays;
}

export function getWeekendsOfMonth(year: number, month: number): Date[] {
  const weekends: number[] = WEEKENDS;
  const retDays = getSpecificWeekdaysOfMonth(year, month, weekends);
  return retDays;
}

export function getWeekdaysOfMonth(year: number, month: number): Date[] {
  const weekdays: number[] = WEEKDAYS;
  const retDays = getSpecificWeekdaysOfMonth(year, month, weekdays);
  return retDays;
}

export function getEverydayOfMonth(year: number, month: number): Date[] {
  const retDays: Date[] = [];
  const targetMonth = month - 1; // Date は 0〜11 なので -1 する

  const date = new Date(year, targetMonth, 1); // 月の最初の日

  // 月の最初の日から月の最後の日までループ
  while (date.getMonth() === targetMonth) {
    retDays.push(new Date(date));
    date.setDate(date.getDate() + 1);
  }

  return retDays;
}

/**
 * 作っておく必要はないが、
 * 他の関数が月1~12なので、それに合わせて月の入力を1~12の関数を作っておく
 */
export function getSingleDayOfMonth(
  year: number,
  month: number,
  day: number
): Date | null {
  const targetMonth = month - 1; // Date は 0〜11 なので -1 する
  const date = new Date(year, targetMonth, day);

  // 日付が正しいかチェック
  if (date.getMonth() === targetMonth && date.getDate() === day) {
    return date;
  }

  return null; // 無効な日付の場合は null を返す
}

/**
 * Date[]の配列に時間をセットする
 */
export function setTimeToDates(
  dates: Date[],
  hour: number,
  minute: number
): Date[] {
  return dates.map((date) => {
    const newDate = new Date(date); // コピーを作る
    newDate.setHours(hour, minute, 0, 0);
    return newDate;
  });
}
