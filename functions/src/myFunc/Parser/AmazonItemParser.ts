import { logger } from "firebase-functions";
import { Expense } from "../../type/Expense";
import { convertUnixMillisecToDateString } from "../utility/getCurrentUnixSec";

export class AmazonItemParser {
  constructor(private rawText: string, private internalDate: string) {}

  extractDate(): string | null {
    const milliSec = Number(this.internalDate);
    const dateStr = convertUnixMillisecToDateString(milliSec);
    return dateStr;
  }

  toExpenses(): Expense[] {
    /**
     * 2パターンくらいある。
     */
    /**
     * パターン1
     * 商品名が表示される。金額は合計はamazonギフトカードが使われるとその分だけ引かれた額になる。
     *
     * * [M2Ma] ランニング キャップ メンズ 水洗いできる 速乾 メッシュキャップ 快適 通気 スポーツキャップ UVカット 帽子 (JP, 数字サイズ, 56.0 cm, 60.0 cm, ホワイト)
     *    数量: 1
     *    1599 JPY
     *
     *   合計
     *   0 JPY
     */

    /**
     * パターン2
     * こちらの場合は商品名が表示されない。
     * 
     *  _________________________________________________________________________________
     *        注文合計： ￥ 907

     *        支払い方法
     *        クレジットカード（Amazon Mastercard）： ￥ 907
     * =================================================================================
     */

    /**
     * ポイントとかあるかもしれないが、とりあえずは全部正規の値段で書いて、
     * 必要に応じてユーザーに金額調整してもらうのがいいか。
     */
    /**
     * まずはパターン1で探してみる。なければ次。
     */
    const productNameExistRegex =
      /\*\s*(.+?)\n\s*数量:\s*(\d+)\s*\n\s*(\d+)\s*JPY/g;
    const expensesPat1 = Array.from(
      this.rawText.matchAll(productNameExistRegex)
    ).map((match) => {
      const name = match[1].trim();
      const price = parseInt(match[3]);
      const expense: Expense = {
        datetime: this.extractDate() ?? undefined,
        amount: price,
        itemName: name,
      };
      logger.info(`expense from Amazon Item:${JSON.stringify(expense)}`);
      return expense;
    });

    if (expensesPat1.length > 0) {
      return expensesPat1;
    }

    /* パターン2で調べてみる */
    const nonProductNameRegex = /注文合計：\s*￥\s*([\d,]+)/g;
    const expensesPat2: Expense[] = Array.from(
      this.rawText.matchAll(nonProductNameRegex)
    ).map((match) => {
      const amount = parseInt(match[1].replace(/,/g, ""), 10)
        ? parseInt(match[1].replace(/,/g, ""), 10)
        : undefined;
      const expense: Expense = {
        datetime: this.extractDate() ?? undefined,
        amount: amount ?? undefined,
      };
      logger.info(`expense from Amazon Item:${JSON.stringify(expense)}`);
      return expense;
    });

    // if (expensesPat2.length > 0) {
    // }

    return expensesPat2;
  }
}
