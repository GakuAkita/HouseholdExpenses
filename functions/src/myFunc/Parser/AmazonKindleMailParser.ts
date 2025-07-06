import { logger } from "firebase-functions";
import { TimeZone } from "../../constants/TimeZone";
import { Expense } from "../../type/Expense";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { convertToUtcIsoString } from "../utility/dateConverter";

/**
 * Kindleのフォーマットはしょっちゅう変わるから
 * 定期的にこのParserを更新する必要あり。
 */
export class AmazonKindleMailParser {
  constructor(private rawText: string) {}

  extractOrderDate(): string | null {
    const match = this.rawText.match(
      /注文日:\s*(\d{4})年(\d{1,2})月(\d{1,2})日(?:\([^)]*\))?/
    );
    if (!match) return null;

    // match[1] = 年, match[2] = 月, match[3] = 日
    const year = match[1];
    const month = ("0" + match[2]).slice(-2); // 先頭に0をつけて後ろ2文字を取得
    const day = ("0" + match[3]).slice(-2);

    const isoStr = `${year}-${month}-${day}T00:00:00`; /* これは日本時間なのでUTCに変換しておく */
    logger.debug(`${isoStr}`);
    const date = new Date(isoStr);

    if (isNaN(date.getTime())) {
      logger.error(`Invalid Date:`, isoStr);
      return null;
    }
    return convertToUtcIsoString(date, TimeZone.JST);
  }

  extractBookTitle(): string | null {
    const match = this.rawText.match(/\n\n(.+?)\n販売者：/);
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
    const orderDate = this.extractOrderDate();
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
