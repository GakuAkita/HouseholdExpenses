import { logger } from "firebase-functions";
import { Expense } from "../../type/Expense";
import { MailParseError, MailParserBase } from "./MailParserBase";

export class UdemyMailParser extends MailParserBase {
  constructor(rawText: string, internalDate: string) {
    super(rawText, internalDate);
  }

  toExpenses(): Expense[] {
    /**
     * 日本語番と英語版がある。
     * プロフィールの設定によって変わるから両方対応しないといけない
     */

    /**
     * 英語版も日本語版も正規表現は同じのを使えそうだ、、、
     */
    // 1. Existing extraction using enJaRegex
    const enJaRegex =
      /^(.*?)\s+List Price:[\s\S]*?Your Price:\s*¥([\d,]+(?:\.\d{2})?)/gm;

    const expensesFromEnJa = Array.from(this.rawText.matchAll(enJaRegex)).map(
      (match) => {
        const lectureName = match[1].trim();
        const priceStr = match[2].replace(/,/g, ""); // Remove commas
        const price = parseInt(priceStr, 10); // Convert to integer

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
      return expensesFromEnJa;
    }

    // 2. New block-based extraction (Course name ... Subtotal/Tax/Credits) as fallback
    const blockRegex = /Course\s+name\r?\nList\s+price\r?\nYour\s+price\r?\n\r?\n([\s\S]*?)(?=(?:Subtotal:|Tax:|Credits:|$))/i;
    const blockMatch = this.rawText.match(blockRegex);

    if (blockMatch) {
      const block = blockMatch[1];
      const courseRegex = /([^\r\n]+)\r?\n(?:¥)?([\d,.]+)\r?\n(?:¥)?([\d,.]+)/g;
      const expenses: Expense[] = [];
      let match;

      while ((match = courseRegex.exec(block)) !== null) {
        const lectureName = match[1].trim();
        const priceStr = match[3].replace(/,/g, ""); // Remove commas
        const price = Math.round(parseFloat(priceStr)); // Consider possibility of decimals

        expenses.push({
          datetime: this.extractDate() ?? undefined,
          amount: price,
          itemName: lectureName,
        });
      }

      if (expenses.length > 0) {
        expenses.forEach((exp) => logger.info(`expense from Udemy (block parser):${JSON.stringify(exp)}`));
        return expenses;
      }
    }

    logger.info("this.rawText:" + this.rawText);
    throw new MailParseError("No expense was extracted from Udemy");
  }
}
