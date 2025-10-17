import { Expense } from "../../type/Expense";
import { MailParserBase } from "./MailParserBase";

export class AmazonItemDispatchedMailParser extends MailParserBase {
  constructor(rawText: string, internalDate: string) {
    super(rawText, internalDate);
  }

  toExpense(): Expense[] {
    return [];
  }
}
