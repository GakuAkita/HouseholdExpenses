import { logger } from "firebase-functions";
import { Expense } from "../../type/Expense";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { MailParserBase } from "./MailParserBase";

export class AmazonItemDispatchedMailParser extends MailParserBase {
  constructor(rawText: string, internalDate: string) {
    super(rawText, internalDate);
  }

  toExpenses(): FuncResultWithData<Expense[]> {
    logger.debug(`${this.rawText}`);

    try {
      const expenses: Expense[] = [];

      // 製品情報を抽出する正規表現
      // パターン: * 製品名\n  数量: X\n  価格 JPY
      const productPattern = /\*\s*([^\n]+)\s*\n\s*数量:\s*(\d+)\s*\n\s*(\d+)\s*JPY/g;

      let match;
      while ((match = productPattern.exec(this.rawText)) !== null) {
        const productName = match[1].trim();
        const quantity = parseInt(match[2], 10);
        const price = parseInt(match[3], 10);

        // 単価を計算（価格 ÷ 数量）
        const unitPrice = Math.round(price / quantity);

        const expense: Expense = {
          datetime: this.extractDate() ?? undefined,
          amount: unitPrice,
          itemName: productName,
          storeName: "Amazon",
          // カテゴリーは後で割り当てられる
        };

        expenses.push(expense);

        logger.debug(`Extracted product: ${productName}, quantity: ${quantity}, total: ${price}, unit: ${unitPrice}`);
      }

      if (expenses.length === 0) {
        return {
          status: FuncStatus.ERROR,
          message: "No products found in the email",
        };
      }

      return {
        status: FuncStatus.SUCCESS,
        message: `Successfully extracted ${expenses.length} products`,
        data: expenses,
      };

    } catch (error: any) {
      logger.error(`Error parsing Amazon dispatched mail: ${error.message}`);
      return {
        status: FuncStatus.ERROR,
        message: `Failed to parse email: ${error.message}`,
      };
    }
  }
}
