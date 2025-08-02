import { logger } from "firebase-functions";
import { Expense } from "../../type/Expense";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { MailParserBase } from "./MailParserBase";

export class UdemyMailParser extends MailParserBase {
  constructor(rawText: string, internalDate: string) {
    super(rawText, internalDate);
  }

  toExpenses(): FuncResultWithData<Expense[]> {
    /**
     * 日本語番と英語版がある。
     * プロフィールの設定によって変わるから両方対応しないといけない
     */

    /**
     * 英語版も日本語版も正規表現は同じのを使えそうだ、、、
     */
    const enJaRegex =
      /^(.*?)\s+List Price:[\s\S]*?Your Price:\s*¥([\d,]+(?:\.\d{2})?)/gm;

    const expensesFromEnJa = Array.from(this.rawText.matchAll(enJaRegex)).map(
      (match) => {
        const lectureName = match[1].trim();
        const priceStr = match[2].replace(/,/g, ""); // カンマ除去
        const price = parseInt(priceStr, 10); // 整数に変換

        const expense: Expense = {
          datetime: this.extractDate() ?? undefined,
          amount: price,
          itemName: lectureName,
        };
        logger.info(`expense from Udemy:${JSON.stringify(expense)}`);
        return expense;
      }
    );

    if (expensesFromEnJa.length > 0) {
      return {
        status: FuncStatus.SUCCESS,
        message: `At least one expense was extracted`,
        data: expensesFromEnJa,
      };
    }

    return {
      status: FuncStatus.ERROR,
      message: "No expense was extracted from Udemy",
    };
  }
}
