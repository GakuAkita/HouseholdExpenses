import { logger } from "firebase-functions";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { AmazonSubscribeItem } from "../../type/Mailbox";

export class AmazonSubscribeNextShipmentMailParser {
  constructor(private rawText: string) { }

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

  extractProductName(): string[] {
    const regex = /([^\r\n]+)\r?\n[^\r\n]*ごとのユニット/gm;
    const matches = [...this.rawText.matchAll(regex)].map((m) => m[1]);
    if (matches.length === 0) return [];

    // 末尾の「...」だけ削除（全角省略記号「…」にも対応）
    const products = matches.map((product) =>
      product.replace(/(?:\.{3}|…)\s*$/, "").trim()
    );
    logger.debug(`Extracted product names: ${products}`);
    return products;
  }

  extractPrice(): number | null {
    /**
     * 単一の製品なら大丈夫だが、rawTextに複数の製品がある場合、注文合計は各商品の価格にはもちろんならない。
     * rawTextから取りたいのだが、どうやら取れないっぽい。したがって、Expenseを作るときは
     */
    // 注文合計(税込) の取得
    const totalMatch = this.rawText.match(/注文合計\s*\(税込\)\s*￥([\d,]+)/);
    const orderTotal = totalMatch
      ? parseInt(totalMatch[1].replace(/,/g, ""), 10)
      : null;
    return orderTotal;
  }

  extractQuantity(): number[] {
    const regex = /([^\r\n]+)\r?\n[^\r\n]*(\d+)\s*ごとのユニット/gm;
    const matches = [...this.rawText.matchAll(regex)];
    if (matches.length === 0) return [];

    const quantities = matches.map((m) => {
      const quantity = parseInt(m[2], 10);
      return isNaN(quantity) ? 1 : quantity;
    });
    return quantities;
  }

  toSubscribeItem(): FuncResultWithData<AmazonSubscribeItem[]> {
    const productNames = this.extractProductName();
    const price = this.extractPrice();
    const quantities = this.extractQuantity();

    if (productNames.length === 0 || !price) {
      return {
        status: FuncStatus.ERROR,
        message: `ProductNames:${productNames.length} , Price:${price} are invalid.`,
      };
    }

    // 製品名と数量の数が一致しない場合は、数量の数に合わせるか、デフォルト値を使う
    const items: AmazonSubscribeItem[] = productNames.map((productName, index) => {
      const quantity = quantities[index] ?? 1;
      return {
        productName: productName,
        price: price,
        quantity: quantity,
      };
    });

    return {
      status: FuncStatus.SUCCESS,
      data: items,
    };
  }
}
