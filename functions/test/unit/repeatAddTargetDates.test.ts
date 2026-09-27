import { describe, expect, it } from "vitest";
import { RepeatFrequency } from "../../src/constants/RepeatFrequency";
import { TimeZone } from "../../src/constants/TimeZone";
import { getRepeatAddTargetDates } from "../../src/domain/repeatAddTargetDates";
import { Frequency } from "../../src/type/RepeatAdd";

const targets = (frequencyInfo: Frequency, month = 9, filter: Date | null = null) =>
  getRepeatAddTargetDates(frequencyInfo, 2026, month, filter, TimeZone.JST);

const isoDates = (frequencyInfo: Frequency, month = 9) =>
  targets(frequencyInfo, month).map((d) => d.toISOString());

describe("getRepeatAddTargetDates", () => {
  it("every_month returns the day at the given JST time", () => {
    expect(
      isoDates({ frequency: RepeatFrequency.EVERY_MONTH, day: 25, hour: 9, minute: 30 })
    ).toEqual(["2026-09-25T00:30:00.000Z"]);
  });

  it("every_month fails for a day the month doesn't have", () => {
    expect(() => targets({ frequency: RepeatFrequency.EVERY_MONTH, day: 31, hour: 9, minute: 0 })).toThrow();
  });

  it("every_year returns the day only in its month", () => {
    const frequency = { frequency: RepeatFrequency.EVERY_YEAR, month: 12, day: 31, hour: 23, minute: 59 };
    expect(isoDates(frequency, 12)).toEqual(["2026-12-31T14:59:00.000Z"]);
    expect(isoDates(frequency, 9)).toEqual([]);
  });

  it("every_week returns the given days of the week", () => {
    expect(
      isoDates({ frequency: RepeatFrequency.EVERY_WEEK, dayOfWeek: [1, 5], hour: 8, minute: 15 })
    ).toHaveLength(8);
  });

  it("every_week fails without dayOfWeek", () => {
    expect(() => targets({ frequency: RepeatFrequency.EVERY_WEEK, hour: 8, minute: 15 })).toThrow();
  });

  it("weekdays, weekends and everyday cover the month", () => {
    expect(isoDates({ frequency: RepeatFrequency.WEEKDAYS, hour: 7, minute: 0 })).toHaveLength(22);
    expect(isoDates({ frequency: RepeatFrequency.WEEKENDS, hour: 7, minute: 0 })).toHaveLength(8);
    expect(isoDates({ frequency: RepeatFrequency.EVERYDAY, hour: 7, minute: 0 })).toHaveLength(30);
  });

  it("fails without hour or minute", () => {
    expect(() => targets({ frequency: RepeatFrequency.EVERYDAY, minute: 0 })).toThrow();
    expect(() => targets({ frequency: RepeatFrequency.EVERYDAY, hour: 0 })).toThrow();
  });

  it("fails for an unknown frequency", () => {
    expect(() => targets({ frequency: "hourly", hour: 0, minute: 0 })).toThrow();
  });

  it("keeps only dates at or after filter_datetime", () => {
    const result = targets(
      { frequency: RepeatFrequency.EVERYDAY, hour: 0, minute: 0 },
      9,
      new Date("2026-09-29T00:00:00.000Z")
    );
    /* 00:00 JST is 15:00 UTC the day before, so the 29th and 30th in JST remain. */
    expect(result.map((d) => d.toISOString())).toEqual(["2026-09-29T15:00:00.000Z"]);
  });
});
