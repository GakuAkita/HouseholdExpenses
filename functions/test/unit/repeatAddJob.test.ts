import { describe, expect, it } from "vitest";
import { RepeatFrequency } from "../../src/constants/RepeatFrequency";
import { TimeZone } from "../../src/constants/TimeZone";
import { ExpenseService } from "../../src/myFunc/FirestoreService/ExpenseService";
import { RepeatAddService } from "../../src/myFunc/FirestoreService/RepeatAddService";
import { SettingsService } from "../../src/myFunc/FirestoreService/SettingsService";
import { RepeatAddProcessor } from "../../src/myFunc/Processor/RepeatAddProcessor";
import { fixedClock } from "../../src/shared/clock";
import { Expense } from "../../src/type/Expense";
import { FuncStatus } from "../../src/type/FuncStatus";
import { RepeatAdd } from "../../src/type/RepeatAdd";

/* The monthly job runs at 01:00 JST on the 1st, which is still the previous day in UTC. */
const firstOfSeptemberJst = new Date("2026-08-31T16:00:00.000Z");

const setUp = (repeatAdds: Record<string, RepeatAdd>, timeZone: string = TimeZone.JST) => {
  const added: Expense[] = [];
  const repeatAddService = {
    getAllRepeatAdds: async () => ({ status: FuncStatus.SUCCESS, data: repeatAdds }),
  } as unknown as RepeatAddService;
  const expenseService = {
    addExpenseWithId: async (_userId: string, expense: Expense) => {
      /* Firestore stores a copy of the object at the time of the write. */
      added.push({ ...expense });
      return { status: FuncStatus.SUCCESS };
    },
  } as unknown as ExpenseService;
  const settingsService = {
    getUserTimeZone: async () => ({ status: FuncStatus.SUCCESS, data: timeZone }),
  } as unknown as SettingsService;

  const processor = new RepeatAddProcessor(
    repeatAddService,
    expenseService,
    settingsService,
    fixedClock(firstOfSeptemberJst)
  );
  return { processor, added };
};

const monthly = (id: string, day: number): RepeatAdd => ({
  id,
  expense: { amount: 8000, note: "rent" },
  frequencyInfo: { frequency: RepeatFrequency.EVERY_MONTH, day, hour: 9, minute: 30 },
});

describe("RepeatAddProcessor.addExpensesFromAllRepeatAdd", () => {
  it("adds this month's expenses, using the month in JST", async () => {
    const { processor, added } = setUp({ repeat1: monthly("repeat1", 25) });

    const result = await processor.addExpensesFromAllRepeatAdd("user1");

    expect(result.status).toBe(FuncStatus.SUCCESS);
    expect(added).toEqual([
      {
        amount: 8000,
        note: "rent",
        datetime: "2026-09-25T00:30:00.000Z",
        generatedType: "repeat_add___repeat1",
        timestamp: firstOfSeptemberJst.getTime(),
      },
    ]);
  });

  it("uses the user's time zone for the time of day", async () => {
    const { processor, added } = setUp({ repeat1: monthly("repeat1", 25) }, TimeZone.AMERICAN_EAST);

    await processor.addExpensesFromAllRepeatAdd("user1");

    /* 09:30 in New York (EDT, UTC-4) */
    expect(added.map((e) => e.datetime)).toEqual(["2026-09-25T13:30:00.000Z"]);
  });

  it("adds one expense per target day", async () => {
    const { processor, added } = setUp({
      repeat1: {
        id: "repeat1",
        expense: { amount: 300 },
        frequencyInfo: { frequency: RepeatFrequency.WEEKENDS, hour: 12, minute: 0 },
      },
    });

    await processor.addExpensesFromAllRepeatAdd("user1");

    expect(added.map((e) => e.datetime)).toEqual([
      "2026-09-05T03:00:00.000Z",
      "2026-09-06T03:00:00.000Z",
      "2026-09-12T03:00:00.000Z",
      "2026-09-13T03:00:00.000Z",
      "2026-09-19T03:00:00.000Z",
      "2026-09-20T03:00:00.000Z",
      "2026-09-26T03:00:00.000Z",
      "2026-09-27T03:00:00.000Z",
    ]);
  });

  it("skips a RepeatAdd whose date doesn't exist this month and continues", async () => {
    const { processor, added } = setUp({
      broken: monthly("broken", 31),
      repeat1: monthly("repeat1", 25),
    });

    const result = await processor.addExpensesFromAllRepeatAdd("user1");

    expect(result.status).toBe(FuncStatus.SUCCESS);
    expect(added.map((e) => e.generatedType)).toEqual(["repeat_add___repeat1"]);
  });
});
