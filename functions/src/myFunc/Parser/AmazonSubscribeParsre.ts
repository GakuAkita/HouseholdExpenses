import { logger } from "firebase-functions";
import { AmazonSubscribeItem } from "../../type/Mailbox";

export class AmazonSubscribeNextShipmentMailParser {
  constructor(private rawText: string) {}

  extractProductName(): string | null {
    const regex = /([^\r\n]+)\r?\n[^\r\n]*ごとのユニット/gm;
    const matches = [...this.rawText.matchAll(regex)].map((m) => m[1]);
    // 末尾の「...」だけ削除（全角省略記号「…」にも対応）
    const product = matches[0].replace(/(?:\.{3}|…)\s*$/, "").trim();
    logger.debug(product);
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

  toSubscribeItem(): AmazonSubscribeItem {
    const productName = this.extractProductName();
    const price = this.extractPrice();
    const quantity = this.extractQuantity();

    return {
      productName: "aa",
      quantity: 1 /* たぶん使わない */,
      price: 0,
    };
  }
}
