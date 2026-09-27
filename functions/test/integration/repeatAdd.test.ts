import * as admin from "firebase-admin";
import { afterEach, describe, expect, it } from "vitest";
import { RepeatFrequency } from "../../src/constants/RepeatFrequency";
import { TimeZone } from "../../src/constants/TimeZone";
import { fixedClock } from "../../src/shared/clock";
import { FuncStatus } from "../../src/type/FuncStatus";
import { createTestServices, deleteTestUser, newTestUserId } from "./emulator";

/* The monthly job runs at 01:00 JST on the 1st. */
const now = new Date("2026-08-31T16:00:00.000Z");

describe("RepeatAdd job", () => {
  const userId = newTestUserId();
  const services = createTestServices({ clock: fixedClock(now) });

  afterEach(() => deleteTestUser(userId));

  it("adds this month's expenses that the app can read", async () => {
    await services.settingsService.setUserPreferences(userId, { timeZone: TimeZone.JST });
    await services.repeatAddService.addRepeatAddWithId(userId, {
      expense: {
        amount: 8000,
        category: { id: "category1", timestamp: 1, name: "生活費", enabled: true },
        note: "rent",
      },
      frequencyInfo: { frequency: RepeatFrequency.EVERY_MONTH, day: 25, hour: 9, minute: 30 },
    });

    const result = await services.repeatAddProcessor.addExpensesFromAllRepeatAdd(userId);
    expect(result.status).toBe(FuncStatus.SUCCESS);

    const repeatAdds = await admin.firestore().collection(`users/${userId}/repeat_add`).get();
    const repeatAddId = repeatAdds.docs[0]?.id;
    const expenses = await admin.firestore().collection(`users/${userId}/expenses`).get();

    expect(expenses.docs.map((doc) => doc.data())).toEqual([
      {
        /* The app requires id, datetime, timestamp, amount and generatedType. */
        id: expenses.docs[0].id,
        datetime: "2026-09-25T00:30:00.000Z",
        timestamp: now.getTime(),
        amount: 8000,
        generatedType: `repeat_add___${repeatAddId}`,
        category: { id: "category1", timestamp: 1, name: "生活費", enabled: true },
        note: "rent",
      },
    ]);
  });
});
