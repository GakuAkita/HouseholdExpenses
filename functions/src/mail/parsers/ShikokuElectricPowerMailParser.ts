import { Expense } from "../../type/Expense";
import { MailParseError, MailParserBase } from "./MailParserBase";

export class ShikokuElectricPowerMailParser extends MailParserBase {
  constructor(rawText: string, internalDate: string) {
    super(rawText, internalDate);
  }

  extractAmount(): number | null {
    const match = this.rawText.match(/ご請求金額：([\d,]+)円/);
    if (!match) return null;
    const amountStr = match[1].replace(/,/g, ""); // カンマ除去
    return Number(amountStr);
  }

  toExpense(): Expense {
    const datetime = this.extractDate();
    const amount = this.extractAmount();

    if (amount == null || !datetime) {
      throw new MailParseError(`${this.constructor.name}:::Unable to get Data from RakutenPay mail : amount=${amount} datetime=${datetime}}`);
    }

    const expense: Expense = {
      datetime: datetime,
      amount: amount,
    };

    return expense;
  }
}
