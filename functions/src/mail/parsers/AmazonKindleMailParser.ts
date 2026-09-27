import { Expense } from "../../type/Expense";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { MailParserBase } from "./MailParserBase";

/**
 * Kindleのフォーマットはしょっちゅう変わるから
 * 定期的にこのParserを更新する必要あり。
 */
export class AmazonKindleMailParser extends MailParserBase {
  constructor(rawText: string, internalDate: string) {
    super(rawText, internalDate);
  }

  extractBookTitle(): string | null {
    const match = this.rawText.match(/(?:^|\n)([^\n]+)\n販売者[：:]/);
    return match ? match[1].trim() : null;
  }

  extractTotalAmount(): number | null {
    const match = this.rawText.match(/総計:\s*￥\s*([\d,]+)/);
    return match ? parseInt(match[1].replace(/,/g, ""), 10) : null;
  }

  extractUsedPoints(): number {
    const match = this.rawText.match(/Amazonポイント：\s*-￥\s*([\d,]+)/);
    return match ? parseInt(match[1].replace(/,/g, ""), 10) : 0; // ポイントなしでもOK
  }

  // 小計を抽出する（例: 商品の小計:  ￥ 1,782）
  extractSubtotal(): number | null {
    const match = this.rawText.match(/商品の小計:\s*￥\s*([\d,]+)/);
    return match ? parseInt(match[1].replace(/,/g, ""), 10) : null;
  }

  // キャンペーン割引額を抽出する（例: キャンペーン:  -￥ 0）
  extractCampaign(): number | null {
    const match = this.rawText.match(/キャンペーン:\s*-￥\s*([\d,]+)/);
    return match ? parseInt(match[1].replace(/,/g, ""), 10) : null;
  }

  toExpense(): FuncResultWithData<Expense> {
    const orderDate = this.extractDate();
    const total = this.extractTotalAmount();
    const bookTitle = this.extractBookTitle();
    if (!orderDate || !bookTitle || total === null) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to parse Kindle mail: orderDate=${orderDate}, title=${bookTitle}, total=${total}`,
      };
    }

    const expense: Expense = {
      datetime: orderDate,
      amount: total,
      itemName: bookTitle,
    };

    return {
      status: FuncStatus.SUCCESS,
      message: "KindleメールからExpenseを生成しました",
      data: expense,
    };
  }
}
