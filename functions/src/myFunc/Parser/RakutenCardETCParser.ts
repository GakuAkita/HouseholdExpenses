import { logger } from "firebase-functions";
import { TimeZone } from "../../constants/TimeZone";
import { Expense } from "../../type/Expense";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { convertyyyymmddToUTCIsoString } from "../utility/dateConverter";

export class RakutenCardETCParser {
  constructor(private rawText: string) {}

  etcStr = "ＥＴＣカード売上";

  extractDate(input?: string, timeZone?: TimeZone): string | undefined {
    if (!input) {
      return undefined;
    }
    return convertyyyymmddToUTCIsoString(input, timeZone);
  }

  /**
   * ETCの部分だけ取り出す
   */
  extractETCblock(): string[] {
    const etcRegex =
      /■利用日:[\s\S]*?[\r\n]+■利用先:\s*ＥＴＣカード売上[\r\n]+[\s\S]*?■利用金額:[\s\S]*?円/g;
    const matches = Array.from(this.rawText.matchAll(etcRegex));
    return matches.map((m) => m[0]);
  }

  toExpenses(): FuncResultWithData<Expense[]> {
    const etcBlocks = this.extractETCblock();

    const expenses = etcBlocks
      .map((etcblock) => {
        /* 利用日と利用金額を取る */
        // 利用日だけ抽出
        const dateMatch = etcblock.match(/■利用日:\s*(\d{4}\/\d{2}\/\d{2})/);
        const date = dateMatch ? dateMatch[1] : undefined;

        // 利用金額だけ抽出
        const amountMatch = etcblock.match(
          /■利用金額:\s*([\d,]+)[\s\u3000]*円/
        );

        const amount = amountMatch
          ? parseInt(amountMatch[1].replace(/,/g, ""), 10)
          : undefined;
        if (date && amount) {
          /* なんでうまくいかないのだ。。。 */
          /* amountってIntだよな？\nがケツに入っているのが気になる */
          /**
           * あ、date&&amountではなくて、!date && !amountになってた。。。そりゃだめだわ。
           */
          const dateStr = this.extractDate(date);
          const expense: Expense = {
            datetime: dateStr,
            amount: amount,
            itemName: this.etcStr,
          };
          logger.log(`${JSON.stringify(expense)}`);

          return expense;
        } else {
          logger.error(`date = ${date} || amount=${amount}`);
        }

        /* ここまで来ることは基本ない */
        logger.error(`Something went wrong...[${etcblock}]`);
        return null;
      })
      .filter((e): e is Expense => e !== null); // null を取り除く型ガード;

    return expenses.length > 0
      ? {
          status: FuncStatus.SUCCESS,
          message: "At least one expense was extracted",
          data: expenses,
        }
      : {
          status: FuncStatus.ERROR,
          message: "No expense was extracted from RakutenETC",
        };
  }
}
