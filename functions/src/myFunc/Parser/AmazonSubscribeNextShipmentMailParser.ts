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
    // 「ごとのユニット」を含む行の直前の非空行を製品名として取得
    // 各行を分割して、「ごとのユニット」を含む行の前の行を探す
    const lines = this.rawText.split(/\r?\n/);
    const productNames: string[] = [];

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();
      // 「ごとのユニット」を含む行を見つける
      if (/\d+\s*ごとのユニット/.test(line)) {
        // 前の行を遡って非空行を探す
        for (let j = i - 1; j >= 0; j--) {
          const prevLine = lines[j].trim();
          if (prevLine) {
            // 末尾の「...」だけ削除（全角省略記号「…」にも対応）
            const productName = prevLine.replace(/(?:\.{3}|…)\s*$/, "").trim();
            if (productName) {
              productNames.push(productName);
            }
            break;
          }
        }
      }
    }

    logger.debug(`Extracted product names: ${productNames}`);
    return productNames;
  }

  extractPrice(): (number | null)[] {
    const lines = this.rawText.split(/\r?\n/);
    const prices: (number | null)[] = [];

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();
      // 「ごとのユニット」を含む行を見つける
      if (/\d+\s*ごとのユニット/.test(line)) {
        let foundPrice: number | null = null;

        // 「ごとのユニット」の行以降の数行を探索（最大5行まで）
        for (let j = i + 1; j < Math.min(i + 6, lines.length); j++) {
          const searchLine = lines[j].trim();

          // まず「新価格: ￥X,XXX」のパターンを探す
          const newPriceMatch = searchLine.match(/新価格:\s*￥([\d,]+)/);
          if (newPriceMatch) {
            foundPrice = parseInt(newPriceMatch[1].replace(/,/g, ""), 10);
            break;
          }

          // 「新価格」がない場合、「￥X,XXX」のパターンを探す
          // ただし「前回の購入価格」や「お得」などの行は除外
          if (!foundPrice && !/前回の購入価格|お得|割引/.test(searchLine)) {
            const priceMatch = searchLine.match(/￥([\d,]+)/);
            if (priceMatch) {
              foundPrice = parseInt(priceMatch[1].replace(/,/g, ""), 10);
              // 「新価格」が後にある可能性があるので、もう少し探索を続ける
              // ただし、次の製品名や「配送を管理」などのキーワードが出たら終了
              if (j + 1 < lines.length && !/配送を管理|この配達に含まれる/.test(lines[j + 1].trim())) {
                continue;
              }
              break;
            }
          }
        }

        prices.push(foundPrice);
      }
    }

    logger.debug(`Extracted prices: ${prices}`);
    return prices;
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
    const prices = this.extractPrice();
    const quantities = this.extractQuantity();

    if (productNames.length === 0) {
      return {
        status: FuncStatus.ERROR,
        message: `ProductNames:${productNames.length} are invalid.`,
      };
    }

    // 製品名と数量、価格の数が一致しない場合は、デフォルト値を使う
    const items: AmazonSubscribeItem[] = productNames.map((productName, index) => {
      const quantity = quantities[index] ?? 1;
      const price = prices[index];
      // 価格が取得できない場合は1を設定
      const finalPrice = price !== null && price !== undefined ? price : 1;
      return {
        productName: productName,
        price: finalPrice,
        quantity: quantity,
      };
    });

    return {
      status: FuncStatus.SUCCESS,
      data: items,
    };
  }
}
