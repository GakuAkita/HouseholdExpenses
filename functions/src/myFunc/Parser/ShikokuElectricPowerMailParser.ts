import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { Expense } from "./../../type/Expense";
import { MailParserBase } from "./MailParserBase";

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

  toExpense(): FuncResultWithData<Expense> {
    const datetime = this.extractDate();
    const amount = this.extractAmount();

    if (amount == null || !datetime) {
      return {
        status: FuncStatus.ERROR,
        message: `${this.constructor.name}:::Unable to get Data from RakutenPay mail : amount=${amount} datetime=${datetime}}`,
      };
    }

    const expense: Expense = {
      datetime: datetime,
      amount: amount,
    };

    return {
      status: FuncStatus.SUCCESS,
      message: `Generated Expense from ${this.constructor.name}`,
      data: expense,
    };
  }
}
