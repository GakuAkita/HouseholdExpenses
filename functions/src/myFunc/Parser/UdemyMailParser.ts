import { Expense } from "../../type/Expense";
import { convertUnixMillisecToDateString } from "../utility/getCurrentUnixSec";

export class UdemyMailParser {
  constructor(private rawText: string, private internalDate: string) {}

  extractDate(): string | null {
    const milliSec = Number(this.internalDate);
    const dateStr = convertUnixMillisecToDateString(milliSec);
    return dateStr;
  }

  extractExpenses(): Expense[] {
    return [];
  }
}
