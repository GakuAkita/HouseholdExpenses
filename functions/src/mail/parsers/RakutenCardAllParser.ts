import { MailParseError } from "./MailParserBase";
import { Expense } from "../../type/Expense";
import { convertyyyymmddToUTCIsoString } from "../../shared/dateConverter";

type Charge = Expense & { storeName: string };

/**
 * 楽天カード「カード利用のお知らせ」: one expense per charge, ETC included.
 * Rakuten sends two layouts, depending on the user's mail setting:
 * - text: a "■利用日 / ■利用先 / ■利用金額" block per charge.
 * - HTML: a "date store amount 円" row per charge, followed by a 合計 row per section.
 *   In Gmail's text rendering a row is one line; in stripped HTML each cell is its own line.
 */
export class RakutenCardAllParser {
  constructor(private rawText: string) {}

  /* Topping up 楽天ペイ moves money, it isn't spending. The space between the words varies. */
  private static isNotExpense = (storeName: string) => /^楽天ペイ残高\s*チャージ/.test(storeName);

  private text(): string {
    return this.rawText
      .replace(/\r\n?/g, "\n")
      .split("\n")
      .map((line) => line.trim())
      .join("\n");
  }

  /** The text layout. The text of each charge, from one "■利用日" to the next. */
  private fromTextLayout(text: string): Charge[] {
    /* The part before the first "■利用日" is the greeting. */
    const blocks = text.split(/(?=■利用日)/).filter((block) => block.startsWith("■利用日"));
    return blocks.map((block) => {
      const date = block.match(/^■利用日[:：]\s*(\d{4}\/\d{1,2}\/\d{1,2})/m)?.[1];
      const storeName = block.match(/^■利用先[:：][ \t　]*(.+)$/m)?.[1].trim();
      const amount = block.match(/^■利用金額[:：]\s*([\d,]+)[\s　]*円/m)?.[1];
      return this.toExpense(date, storeName, amount);
    });
  }

  /** The HTML layout. A row starts with the date, so 合計 rows don't match. */
  private fromHtmlLayout(text: string): Charge[] {
    const rowRegex = /^(\d{4}\/\d{1,2}\/\d{1,2})\s+([^\n]+?)\s+([\d,]+)[ \t　]*円$/gm;
    return Array.from(text.matchAll(rowRegex), (m) => this.toExpense(m[1], m[2].trim(), m[3]));
  }

  private toExpense(date?: string, storeName?: string, amount?: string): Charge {
    /* A charge without one of them is not saved, so the whole mail fails instead of saving part of it. */
    if (!date || !storeName || !amount) {
      throw new MailParseError(
        `Unable to read a Rakuten Card charge: date=${date} storeName=${storeName} amount=${amount}`
      );
    }
    return {
      datetime: convertyyyymmddToUTCIsoString(date),
      amount: parseInt(amount.replace(/,/g, ""), 10),
      storeName,
    };
  }

  /** The charges to save. Empty when every charge in the mail is one that isn't saved. */
  toExpenses(): Expense[] {
    const text = this.text();
    const expenses = text.includes("■利用日") ? this.fromTextLayout(text) : this.fromHtmlLayout(text);
    if (expenses.length === 0) {
      throw new MailParseError("No charge was found in the Rakuten Card mail");
    }
    return expenses.filter((expense) => !RakutenCardAllParser.isNotExpense(expense.storeName));
  }
}
