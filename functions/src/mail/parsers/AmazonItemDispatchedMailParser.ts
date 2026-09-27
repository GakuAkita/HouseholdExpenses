import { logger } from "firebase-functions";
import { Expense } from "../../type/Expense";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { MailParserBase } from "./MailParserBase";

export class AmazonItemDispatchedMailParser extends MailParserBase {
  constructor(rawText: string, internalDate: string) {
    super(rawText, internalDate);
  }

  toExpenses(): FuncResultWithData<Expense[]> {
    //logger.debug(`${this.rawText}`);

    try {
      const expenses: Expense[] = [];

      // 製品情報を抽出する正規表現
      // パターン: * 製品名\n  数量: X\n  価格 JPY
      /**
       * この価格は数量が複数だった場合、その合計に鳴るっぽいが、、
       * その場合は単価を計算して、数量分だけ個別のExpenseを作成する。
       * もし違った場合は修正が必要。ただ、複数数量注文することはあまりない
       */
      const productPattern = /\*\s*([^\n]+)\s*\n\s*数量:\s*(\d+)\s*\n\s*(\d+)\s*JPY/g;

      let match;
      while ((match = productPattern.exec(this.rawText)) !== null) {
        const productName = match[1].trim();
        const quantity = parseInt(match[2], 10);
        const price = parseInt(match[3], 10);

        // 単価を計算（価格 ÷ 数量）
        const unitPrice = Math.round(price / quantity);

        // 数量分だけ個別のExpenseを作成
        for (let i = 0; i < quantity; i++) {
          const expense: Expense = {
            datetime: this.extractDate() ?? new Date().toISOString(),
            amount: unitPrice,
            itemName: productName,
            storeName: "Amazon",
            // カテゴリーは後で割り当てられる
          };

          expenses.push(expense);
        }

        logger.debug(`Extracted product: ${productName}, quantity: ${quantity}, total: ${price}, unit: ${unitPrice}, created ${quantity} individual expenses`);
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
