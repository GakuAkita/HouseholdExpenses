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

// 文字列 → 番号
// export const DayOfWeekNameToNum: { [key: string]: DayOfWeekNum } =
//   Object.fromEntries(
//     Object.entries(DayOfWeekLabels).map(([numStr, name]) => [
//       name.toLowerCase(),
//       Number(numStr),
//     ])
//   ) as { [key: string]: DayOfWeekNum };

// export function convertDayNamesToNums(dayNames: number[]): DayOfWeekNum[] {
//   return dayNames
//     .map((name) => DayOfWeekNameToNum[name.toLowerCase()])
//     .filter((num): num is DayOfWeekNum => num !== undefined);
// }
