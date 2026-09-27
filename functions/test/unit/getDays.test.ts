import { describe, expect, it } from "vitest";
import { DayOfWeekNum } from "../../src/constants/DayOfWeek";
import { TimeZone } from "../../src/constants/TimeZone";
import {
  getEverydayOfMonth,
  getSingleDayOfMonth,
  getSpecificWeekdaysOfMonth,
  getWeekdaysOfMonth,
  getWeekendsOfMonth,
  setTimeToDates,
} from "../../src/myFunc/utility/getDays";

const days = (dates: Date[]) => dates.map((d) => d.getDate());

describe("runtime time zone", () => {
  it("is UTC, the same as Cloud Functions", () => {
    expect(new Date(2026, 0, 1).getTimezoneOffset()).toBe(0);
  });
});

describe("getDays", () => {
  /* September 2026 starts on Tuesday and has 30 days. */
  it("getEverydayOfMonth returns every day of the month", () => {
    expect(days(getEverydayOfMonth(2026, 9))).toEqual(
      Array.from({ length: 30 }, (_, i) => i + 1)
    );
  });

  it("getEverydayOfMonth handles a leap year February", () => {
    expect(getEverydayOfMonth(2028, 2)).toHaveLength(29);
  });

  it("getSpecificWeekdaysOfMonth returns the matching days", () => {
    expect(
      days(getSpecificWeekdaysOfMonth(2026, 9, [DayOfWeekNum.MON, DayOfWeekNum.FRI]))
    ).toEqual([4, 7, 11, 14, 18, 21, 25, 28]);
  });

  it("getWeekendsOfMonth and getWeekdaysOfMonth split the month", () => {
    expect(days(getWeekendsOfMonth(2026, 9))).toEqual([5, 6, 12, 13, 19, 20, 26, 27]);
    expect(getWeekdaysOfMonth(2026, 9)).toHaveLength(22);
  });

  it("getSingleDayOfMonth returns null for a day the month doesn't have", () => {
    expect(getSingleDayOfMonth(2026, 9, 30)?.getDate()).toBe(30);
    expect(getSingleDayOfMonth(2026, 9, 31)).toBeNull();
    expect(getSingleDayOfMonth(2026, 2, 29)).toBeNull();
  });

  it("setTimeToDates sets the wall-clock time in the given zone", () => {
    const [date] = setTimeToDates([new Date(2026, 8, 25)], 9, 30, TimeZone.JST);
    expect(date.toISOString()).toBe("2026-09-25T00:30:00.000Z");
  });

  it("setTimeToDates defaults to JST", () => {
    const [date] = setTimeToDates([new Date(2026, 8, 25)], 0, 0);
    expect(date.toISOString()).toBe("2026-09-24T15:00:00.000Z");
  });
});
