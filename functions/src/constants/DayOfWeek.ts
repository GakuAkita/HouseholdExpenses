export enum DayOfWeekNum {
  SUN = 0,
  MON = 1,
  TUE = 2,
  WED = 3,
  THU = 4,
  FRI = 5,
  SAT = 6,
}

export const DayOfWeekLabels: { [key in DayOfWeekNum]: string } = {
  [DayOfWeekNum.SUN]: "Sunday",
  [DayOfWeekNum.MON]: "Monday",
  [DayOfWeekNum.TUE]: "Tuesday",
  [DayOfWeekNum.WED]: "Wednesday",
  [DayOfWeekNum.THU]: "Thursday",
  [DayOfWeekNum.FRI]: "Friday",
  [DayOfWeekNum.SAT]: "Saturday",
};

export const WEEKENDS = [DayOfWeekNum.SUN, DayOfWeekNum.SAT];

export const WEEKDAYS = [
  DayOfWeekNum.MON,
  DayOfWeekNum.TUE,
  DayOfWeekNum.WED,
  DayOfWeekNum.THU,
  DayOfWeekNum.FRI,
];

// 文字列・数値 → TS曜日番号 (0: Sun, 1: Mon, ..., 6: Sat)
export const DayOfWeekNameToNum: Record<string, DayOfWeekNum> = {
  sunday: DayOfWeekNum.SUN,
  sun: DayOfWeekNum.SUN,
  monday: DayOfWeekNum.MON,
  mon: DayOfWeekNum.MON,
  tuesday: DayOfWeekNum.TUE,
  tue: DayOfWeekNum.TUE,
  wednesday: DayOfWeekNum.WED,
  wed: DayOfWeekNum.WED,
  thursday: DayOfWeekNum.THU,
  thu: DayOfWeekNum.THU,
  friday: DayOfWeekNum.FRI,
  fri: DayOfWeekNum.FRI,
  saturday: DayOfWeekNum.SAT,
  sat: DayOfWeekNum.SAT,
};

/**
 * 曜日を表す文字列（"MONDAY", "Monday", "MON", "1"〜"7" など）または数値
 * を TypeScript/JS の Date.getDay() (0: Sunday, 1: Monday, ..., 6: Saturday) の数値に変換します。
 */
export function convertDayToNum(day: string | number): DayOfWeekNum | null {
  if (typeof day === "number") {
    // Kotlinの java.time.DayOfWeek 数値 (1: Mon, ..., 7: Sun)
    if (day === 7) return DayOfWeekNum.SUN;
    if (day >= 1 && day <= 6) return day as DayOfWeekNum;
    // JSの Date.getDay() 数値 (0: Sun, ..., 6: Sat)
    if (day === 0) return DayOfWeekNum.SUN;
    return null;
  }

  if (typeof day === "string") {
    const trimmed = day.trim().toLowerCase();

    // 英語名 ("MONDAY", "Monday", "mon" など)
    if (trimmed in DayOfWeekNameToNum) {
      return DayOfWeekNameToNum[trimmed];
    }

    // 数字の文字列 ("1"〜"7" または "0"〜"6") の場合
    const parsed = parseInt(trimmed, 10);
    if (!isNaN(parsed)) {
      return convertDayToNum(parsed);
    }
  }

  return null;
}

export function convertDaysToNums(days: (string | number)[]): DayOfWeekNum[] {
  return days
    .map((day) => convertDayToNum(day))
    .filter((num): num is DayOfWeekNum => num !== null);
}

