import { Expense } from "../../type/Expense";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { convertUnixMillisecToDateString } from "../utility/getCurrentUnixSec";

export class UdemyMailParser {
  constructor(private rawText: string, private internalDate: string) {}

  extractDate(): string | null {
    const milliSec = Number(this.internalDate);
    const dateStr = convertUnixMillisecToDateString(milliSec);
    return dateStr;
  }

  toExpenses(): FuncResultWithData<Expense[]> {
    return {
      status: FuncStatus.ERROR,
      message: "No expense was extracted from Udemy",
    };
  }
}
