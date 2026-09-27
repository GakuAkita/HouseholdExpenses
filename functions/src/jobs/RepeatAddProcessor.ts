import { logger } from "firebase-functions";
import { DateTime } from "luxon";
import { GeneratedType } from "../constants/GeneratedType";
import { TriggerTimeZone } from "../constants/TimeZone";
import { getRepeatAddTargetDates } from "../domain/repeatAddTargetDates";
import { ExpenseService } from "../infra/firestore/ExpenseService";
import { RepeatAddService } from "../infra/firestore/RepeatAddService";
import { SettingsService } from "../infra/firestore/SettingsService";
import { Clock } from "../shared/clock";
import { messageOf } from "../shared/errors";
import { RepeatAdd } from "../type/RepeatAdd";

export class RepeatAddProcessor {
  constructor(
    private repeatAddService: Pick<RepeatAddService, "getAllRepeatAdds">,
    private expenseService: Pick<ExpenseService, "addExpenseWithId">,
    private settingsService: Pick<SettingsService, "getUserTimeZone">,
    private clock: Clock
  ) {}

  /** Adds the RepeatAdd's expense for the date. The RepeatAdd itself is not changed. */
  async addExpenseFromRepeatAdd(userId: string, repeatAdd: RepeatAdd, targetDate: Date): Promise<void> {
    if (repeatAdd.expense == null) {
      throw new Error(`Expense is null for repeat add ${repeatAdd.id}.`);
    }
    await this.expenseService.addExpenseWithId(userId, {
      ...repeatAdd.expense,
      /* ___(アンダーバー3つ)を区切りサインとする */
      generatedType: `${GeneratedType.REPEAT_ADD}___${repeatAdd.id}`,
      timestamp: this.clock.now().getTime(),
      /* targetDateは、UTCに変換したときに設定のタイムゾーンで設定の時間になるようにしてある */
      datetime: targetDate.toISOString(),
    });
  }

  /**
   * あるユーザーのrepeatAddをすべて取得して、各repeatAddからexpenseを追加していく。
   * A RepeatAdd that fails is logged and skipped. Returns the number of expenses added.
   */
  async addExpensesFromAllRepeatAdd(userId: string): Promise<number> {
    logger.log("Processing user ID:", userId);
    const repeatAdds = await this.repeatAddService.getAllRepeatAdds(userId);
    logger.log(`Found ${Object.keys(repeatAdds).length} repeat adds.`);

    /* 設定のタイムゾーンを取得してくる */
    const userTimeZone = await this.settingsService.getUserTimeZone(userId);
    logger.log(`User time zone: ${userTimeZone}`);

    /* The job runs on the 1st in Japan time, so the month is taken in Japan time. */
    const triggerRegionTime = DateTime.fromJSDate(this.clock.now()).setZone(TriggerTimeZone);
    const currentYear = triggerRegionTime.year;
    const currentMonth = triggerRegionTime.month; /* 1~12 */
    logger.log(`Current year: ${currentYear}, month: ${currentMonth}`);

    let totalAdded = 0;
    for (const repeatAdd of Object.values(repeatAdds)) {
      let targetDates: Date[];
      try {
        targetDates = getRepeatAddTargetDates(
          repeatAdd.frequencyInfo,
          currentYear,
          currentMonth,
          null,
          userTimeZone,
          repeatAdd.id
        );
      } catch (error) {
        logger.error(`Failed to get target dates for repeat add ${repeatAdd.id}: ${messageOf(error)}`);
        continue;
      }

      let added = 0;
      for (const targetDate of targetDates) {
        try {
          await this.addExpenseFromRepeatAdd(userId, repeatAdd, targetDate);
          added++;
        } catch (error) {
          logger.error(
            `Failed to add expense for repeat add ${repeatAdd.id} on ${targetDate.toISOString()}: ${messageOf(error)}`
          );
        }
      }
      if (added !== targetDates.length) {
        logger.warn(
          `Not all expenses were added for repeat add ${repeatAdd.id}. Added: ${added}, Target: ${targetDates.length}`
        );
      }
      totalAdded += added;
    }
    return totalAdded;
  }
}
