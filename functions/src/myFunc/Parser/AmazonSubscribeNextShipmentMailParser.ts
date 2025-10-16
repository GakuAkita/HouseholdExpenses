import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { AmazonSubscribeItem } from "../../type/Mailbox";

export class AmazonSubscribeNextShipmentMailParser {
  constructor(private rawText: string) {}

  /**
    ..様、こんにちは
    次回の定期おトク便の自動配信が間近に迫っています。定期おトク便商品を確認してください。
    (www.amazon.com)

    注文の詳細

    注文合計 (税込)  ￥1,189
    割引合計  -￥63


    月曜日, 10/20 までに到着予定

    次回のお届けを管理できる最終日：  水曜日, 10/15


    ..に配達予定
    この配達に含まれる1点の商品

    (url)

    by Amazon 天然水 ラベルレス 500ml ×24本 富士山の天然水 バナジウム含有 水...
    1 ごとのユニット 2 週
    
    配送を管理する (url)
   */

  extractProductName(): string | null {
    const regex = /([^\r\n]+)\r?\n[^\r\n]*ごとのユニット/gm;
    const matches = [...this.rawText.matchAll(regex)].map((m) => m[1]);
    if (matches.length === 0) return null;

    // 末尾の「...」だけ削除（全角省略記号「…」にも対応）
    const product = matches[0].replace(/(?:\.{3}|…)\s*$/, "").trim();
    return product;
  }

  extractPrice(): number | null {
    // 注文合計(税込) の取得
    const totalMatch = this.rawText.match(/注文合計\s*\(税込\)\s*￥([\d,]+)/);
    const orderTotal = totalMatch
      ? parseInt(totalMatch[1].replace(/,/g, ""), 10)
      : null;
    return orderTotal;
  }

  extractQuantity(): number {
    const match = this.rawText.match(/(\d+)\s*ごとのユニット/);
    const quantity = match ? parseInt(match[1], 10) : 1;
    return quantity;
  }

  toSubscribeItem(): FuncResultWithData<AmazonSubscribeItem> {
    const productName = this.extractProductName();
    const price = this.extractPrice();
    const quantity = this.extractQuantity();

    if (!productName || !price || !quantity) {
      return {
        status: FuncStatus.ERROR,
        message: `ProductName${productName} , Price:${price}, Quantity:${quantity} are invalid.`,
      };
    } else {
      const item: AmazonSubscribeItem = {
        productName: productName,
        price: price,
        quantity: quantity,
      };
      return {
        status: FuncStatus.SUCCESS,
        data: item,
      };
    }
  }
}
