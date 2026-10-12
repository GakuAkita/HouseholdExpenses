import { logger } from "firebase-functions";
import { AmazonSubscribeItem } from "../../type/Mailbox";
import { MailParseError } from "./MailParserBase";

/**
 * Amazonの定期便の初回は普通にAmazonItemの方で追加される。一度届く。
 * 2回目以降に、"次回の配達をご確認ください"系のメールが届くので、そこで初めて登録する。でないと、最初の購入の時点と含めてダブルで検知してしまう可能性があるため。
 */
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

  /**
   * One price per item, or null when the item has none.
   * The text from the HTML mail has a blank line between lines, so blank lines are skipped:
   *
   *   1 ごとのユニット 2 週
   *   前回の購入価格: ￥1,245      (price-change mails only)
   *   (5% 割引)
   *   新価格: ￥1,401
   *   (5% 割引)
   *
   * 「新価格」 wins. Otherwise the first ￥ amount that isn't the previous price or a discount.
   */
  extractPrice(): (number | null)[] {
    const prices = this.itemBlocks().map((block) => {
      let foundPrice: number | null = null;
      for (const line of block) {
        const newPriceMatch = line.match(/新価格:\s*￥([\d,]+)/);
        if (newPriceMatch) {
          return parseInt(newPriceMatch[1].replace(/,/g, ""), 10);
        }
        // 「前回の購入価格」や「お得」などの行は除外
        if (foundPrice === null && !/前回の購入価格|お得|割引/.test(line)) {
          const priceMatch = line.match(/￥([\d,]+)/);
          if (priceMatch) {
            foundPrice = parseInt(priceMatch[1].replace(/,/g, ""), 10);
          }
        }
      }
      return foundPrice;
    });

    logger.debug(`Extracted prices: ${prices}`);
    return prices;
  }

  /** One quantity per item: the number before 「ごとのユニット」 (1 when missing). */
  extractQuantity(): number[] {
    return this.unitLineIndexes().map((i) => {
      const quantity = parseInt(this.lines()[i].match(/(\d+)\s*ごとのユニット/)?.[1] ?? "", 10);
      return isNaN(quantity) ? 1 : quantity;
    });
  }

  private lines(): string[] {
    return this.rawText.split(/\r?\n/).map((line) => line.trim());
  }

  /** The index of each item's 「ごとのユニット」 line. */
  private unitLineIndexes(): number[] {
    return this.lines().flatMap((line, i) => (/\d+\s*ごとのユニット/.test(line) ? [i] : []));
  }

  /**
   * The non-empty lines after each item's 「ごとのユニット」 line, up to the next item,
   * 「配送を管理」 or 「この配達に含まれる」, and at most 8 lines.
   */
  private itemBlocks(): string[][] {
    const lines = this.lines();
    return this.unitLineIndexes().map((start) => {
      const block: string[] = [];
      for (let j = start + 1; j < lines.length && block.length < 8; j++) {
        const line = lines[j];
        if (!line) continue;
        if (/\d+\s*ごとのユニット|配送を管理|この配達に含まれる/.test(line)) break;
        block.push(line);
      }
      return block;
    });
  }

  toSubscribeItem(): AmazonSubscribeItem[] {
    const productNames = this.extractProductName();
    const prices = this.extractPrice();
    const quantities = this.extractQuantity();

    if (productNames.length === 0) {
      throw new MailParseError(
        `ProductNames:${productNames.length} are invalid.`,
      );
    }

    // 製品名と数量、価格の数が一致しない場合は、デフォルト値を使う
    const items: AmazonSubscribeItem[] = productNames.map(
      (productName, index) => {
        const quantity = quantities[index] ?? 1;
        const price = prices[index];
        // 価格が取得できない場合は1を設定
        const finalPrice = price !== null && price !== undefined ? price : 1;
        return {
          productName: productName,
          price: finalPrice,
          quantity: quantity,
        };
      },
    );

    return items;
  }
}
