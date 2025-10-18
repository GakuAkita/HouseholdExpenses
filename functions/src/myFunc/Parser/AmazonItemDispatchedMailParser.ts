import { Expense } from "../../type/Expense";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { MailParserBase } from "./MailParserBase";

export class AmazonItemDispatchedMailParser extends MailParserBase {
  constructor(rawText: string, internalDate: string) {
    super(rawText, internalDate);
  }

  toExpenses(): FuncResultWithData<Expense[]> {
    return {
      status: FuncStatus.ERROR,
      message: `Not implemeneted yet.`,
    };
  }
}
