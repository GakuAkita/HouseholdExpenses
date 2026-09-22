import { logger } from "firebase-functions";
import { DateTime } from "luxon";
import { TimeZone } from "../../constants/TimeZone";
import { Expense } from "../../type/Expense";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";

export class RakutenPayMailParser {
  constructor(private rawText: string) {}

  extractDate(): string | null {
    const patterns = [
      /(?:▼|▽)?(?:ご利用日時|お支払い日時|決済日時|日時)\s+(\d{4}[\/\-]\d{2}[\/\-]\d{2}.*?\d{2}:\d{2})/,
    ];
    let dateStr: string | null = null;
    for (const pattern of patterns) {
      const match = this.rawText.match(pattern);
      if (match) {
        dateStr = match[1].trim();
        break;
      }
    }
    if (!dateStr) {
      return null;
    }

    const cleaned = dateStr.replace(/\([^\)]*\)/g, ""); // "(曜日)" を削除
    const r10TimeStr = cleaned.trim().replace(/\//g, "-"); // "2026-08-19 19:04"

    /**
     * 日本時間として捉えて、それのUTCを取る
     */
    const date = DateTime.fromFormat(r10TimeStr, "yyyy-MM-dd HH:mm", {
      zone: TimeZone.JST,
    });
    //logger.log("This is before conversion ->", date);

    if (!date.isValid) {
      return null;
    }

    const iso = date.toUTC().toISO();

    //logger.log("This is after conversion ->", iso);

    /* 一度Dateに変換して、それからUTCにconvertする */
    return iso;
  }

  extractAmount(): number | null {
    // 決済総額 / お支払い合計 / お支払金額 / お支払い金額 / 決済金額 / 合計金額 / ご利用金額 / 支払総額 などに対応 (￥, ¥, 円表記)
    const patterns = [
      /(?:▼|▽)?(?:決済総額|お支払い合計|お支払(?:い)?金額|お支払い総額|決済金額|合計金額|ご利用金額|支払総額)\s+[￥¥\\]?\s*([\d,]+)\s*(?:円)?/,
      /(?:▼|▽)?合計\s+[￥¥\\]?\s*([\d,]+)\s*(?:円)?/,
    ];
    for (const pattern of patterns) {
      const match = this.rawText.match(pattern);
      if (match) {
        return parseInt(match[1].replace(/,/g, ""), 10);
      }
    }
    return null;
  }

  extractStoreName(): string | null {
    const patterns = [
      /(?:▼|▽)?(?:ご利用店舗|利用店舗|ご利用先|お支払先|加盟店)\s+(.+)/,
    ];
    for (const pattern of patterns) {
      const match = this.rawText.match(pattern);
      if (match) {
        return match[1].trim();
      }
    }
    return null;
  }

  extractUsedPoint(): number | null {
    const patterns = [
      /(?:▼|▽)?(?:楽天ポイント・キャッシュ利用額|ポイント・キャッシュ利用額|ポイント・キャッシュ利用|楽天ポイント利用|楽天ポイント|ポイント)\s+[￥¥\\]?\s*([\d,]+)\s*(?:円|pt|ポイント)?/,
    ];
    for (const pattern of patterns) {
      const match = this.rawText.match(pattern);
      if (match) {
        return parseInt(match[1].replace(/,/g, ""), 10);
      }
    }
    return null;
  }

  toExpense(): FuncResultWithData<Expense> {
    const amount = this.extractAmount();
    const storeName = this.extractStoreName();
    const datetime = this.extractDate();
    const usedPoint = this.extractUsedPoint() ?? 0;
    //logger.debug(`${this.rawText}`);

    if (amount === null || !storeName || !datetime) {
      return {
        status: FuncStatus.ERROR,
        message: `Unable to get Data from RakutenPay mail : amount=${amount} storeName=${storeName} datetime=${datetime} usedPoint=${usedPoint}`,
      };
    }

    const netAmount = amount - usedPoint;
    if (netAmount < 0) {
      return {
        status: FuncStatus.ERROR,
        message: `Net amount is negative ${netAmount}yen (amount:${amount}, usedPoint:${usedPoint})`,
      };
    } else if (netAmount == 0) {
      logger.info(`amount(${amount}) - point(${usedPoint}) = netAmount 0`);
    }

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
