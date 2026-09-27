import { describe, expect, it } from "vitest";
import { RepeatFrequency } from "../../src/constants/RepeatFrequency";
import { TimeZone } from "../../src/constants/TimeZone";
import { RepeatAddProcessor } from "../../src/myFunc/Processor/RepeatAddProcessor";
import { FuncStatus } from "../../src/type/FuncStatus";
import { Frequency, RepeatAdd } from "../../src/type/RepeatAdd";

/* getTargetDateFromRepeatAdd doesn't touch the services. */
const processor = new RepeatAddProcessor({} as never, {} as never, {} as never);

const repeatAdd = (frequencyInfo: Frequency): RepeatAdd => ({
  id: "repeat1",
  expense: { amount: 8000 },
  frequencyInfo,
});

const targets = (frequencyInfo: Frequency, month = 9, filter: Date | null = null) =>
  processor.getTargetDateFromRepeatAdd(repeatAdd(frequencyInfo), 2026, month, filter, TimeZone.JST);

const isoDates = (frequencyInfo: Frequency, month = 9) =>
  targets(frequencyInfo, month).data?.map((d) => d.toISOString());

describe("RepeatAddProcessor.getTargetDateFromRepeatAdd", () => {
  it("every_month returns the day at the given JST time", () => {
    expect(
      isoDates({ frequency: RepeatFrequency.EVERY_MONTH, day: 25, hour: 9, minute: 30 })
    ).toEqual(["2026-09-25T00:30:00.000Z"]);
  });

  it("every_month fails for a day the month doesn't have", () => {
    const result = targets({ frequency: RepeatFrequency.EVERY_MONTH, day: 31, hour: 9, minute: 0 });
    expect(result.status).toBe(FuncStatus.ERROR);
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
    const result = targets({ frequency: RepeatFrequency.EVERY_WEEK, hour: 8, minute: 15 });
    expect(result.status).toBe(FuncStatus.ERROR);
  });

  it("weekdays, weekends and everyday cover the month", () => {
    expect(isoDates({ frequency: RepeatFrequency.WEEKDAYS, hour: 7, minute: 0 })).toHaveLength(22);
    expect(isoDates({ frequency: RepeatFrequency.WEEKENDS, hour: 7, minute: 0 })).toHaveLength(8);
    expect(isoDates({ frequency: RepeatFrequency.EVERYDAY, hour: 7, minute: 0 })).toHaveLength(30);
  });

  it("fails without hour or minute", () => {
    expect(targets({ frequency: RepeatFrequency.EVERYDAY, minute: 0 }).status).toBe(FuncStatus.ERROR);
    expect(targets({ frequency: RepeatFrequency.EVERYDAY, hour: 0 }).status).toBe(FuncStatus.ERROR);
  });

  it("fails for an unknown frequency", () => {
    expect(targets({ frequency: "hourly", hour: 0, minute: 0 }).status).toBe(FuncStatus.ERROR);
  });

  it("keeps only dates at or after filter_datetime", () => {
    const result = targets(
      { frequency: RepeatFrequency.EVERYDAY, hour: 0, minute: 0 },
      9,
      new Date("2026-09-29T00:00:00.000Z")
    );
    /* 00:00 JST is 15:00 UTC the day before, so the 29th and 30th in JST remain. */
    expect(result.data?.map((d) => d.toISOString())).toEqual([
      "2026-09-29T15:00:00.000Z",
    ]);
  });
});
