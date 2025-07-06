import { logger } from "firebase-functions";
import { TimeZone } from "../../constants/TimeZone";
import { Expense } from "../../type/Expense";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { createRakutenPaySettingInstance } from "../../type/Mailbox";
import { convertToUtcIsoString } from "../utility/dateConverter";

export class RakutenPayMailParser {
  constructor(private rawText: string) {}

  extractDate(): string | null {
    const match = this.rawText.match(
      /ご利用日時\s+(\d{4}\/\d{2}\/\d{2}.*?\d{2}:\d{2})/
    );
    /*2023/04/20(木) 19:14*/

    const dateStr = match ? match[1].trim() : null;
    if (!dateStr) {
      return dateStr;
    }

    const cleaned = dateStr.replace(/\([^\)]*\)/g, ""); // "(曜日)" を削除
    const isoLike = cleaned.trim().replace(/\//g, "-"); // "2023-04-20 19:14"

    const date = new Date(isoLike);

    /* 日本時間なので、UTCに直さないといけない */
    const iso = convertToUtcIsoString(date, TimeZone.JST);

    /* 一度Dateに変換して、それからUTCにconvertする */
    return iso;
  }

  extractAmount(): number | null {
    const match = this.rawText.match(/決済総額\s+([\d,]+)円/);
    return match
      ? parseInt(match[1].replace(/,/g, ""), 10 /* 10進数基底 */)
      : null;
  }

  extractStoreName(): string | null {
    const match = this.rawText.match(/ご利用店舗\s+(.+)/);
    return match ? match[1].trim() : null;
  }

  extractUsedPoint(): number | null {
    const match = this.rawText.match(/ポイント\s+([\d,]+)/);
    return match ? parseInt(match[1].replace(/,/g, ""), 10) : null;
  }

  toExpense(): FuncResultWithData<Expense> {
    const amount = this.extractAmount();
    const storeName = this.extractStoreName();
    const datetime = this.extractDate();
    const usedPoint = this.extractUsedPoint();
    if (amount === null || !storeName || !datetime || !usedPoint) {
      return {
        status: FuncStatus.ERROR,
        message: `Unable to get Data from RakutenPay mail : amount=${amount} storeName=${storeName} datetime=${datetime} userdPoint=${usedPoint}`,
      };
    }

    const netAmount = amount - usedPoint;
    if (netAmount < 0) {
      return {
        status: FuncStatus.ERROR,
        message: `Net amount is negative ${netAmount}yen`,
      };
    } else if (netAmount == 0) {
      logger.info(`amount(${amount}) - point(${usedPoint}) = netAmount 0`);
    }

    const rakutenSample = createRakutenPaySettingInstance({
      enabled: true,
    });

    const expense: Expense = {
      datetime: datetime,
      amount: netAmount /* ポイント使った分は引く */,
      storeName: storeName,
    };

    return {
      status: FuncStatus.SUCCESS,
      message: "Generated Expense from RakutenPay",
      data: expense,
    };
  }
}
