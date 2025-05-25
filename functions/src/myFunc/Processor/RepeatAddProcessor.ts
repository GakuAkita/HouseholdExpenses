// RepeatAddProcessor.ts (または適切なファイル名)
import { FuncResult, FuncStatus } from "../../type/FuncStatus";
import { ExpenseService } from "../FirestoreService/ExpenseService";
import { RepeatAddService } from "../FirestoreService/RepeatAddService";

export class RepeatAddProcessor {
  constructor(
    private repeatAddService: RepeatAddService,
    private expenseService: ExpenseService
  ) {}

  async processWeeklyRepeatAddsForMonth(
    userId: string,
    year: number,
    month: number
  ): Promise<FuncResult> {
    const repeatAddsResult = await this.repeatAddService.getAllRepeatAdds(
      userId
    );
    if (
      repeatAddsResult.status !== FuncStatus.SUCCESS ||
      !repeatAddsResult.data
    ) {
      const ret: FuncResult = {
        status: repeatAddsResult.status,
        message: repeatAddsResult.message || "Failed to fetch repeat adds",
      };
      return ret;
    }

    const repeatAdds = repeatAddsResult.data;

    // 今月の曜日ごとに該当するevery_weekのrepeatAddをフィルターしてexpenseを追加する処理
    for (const [_, repeatAdd] of Object.entries(repeatAdds)) {
      if (repeatAdd.frequencyInfo.frequency === "every_week") {
        // ここで曜日判定してExpenseを追加するロジックを入れる
        // this.expenseService.addExpense(...) を呼び出す
      }
    }
  }
}
