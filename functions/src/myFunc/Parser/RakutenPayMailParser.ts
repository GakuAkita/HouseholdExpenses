import { Expense } from "../../type/Expense";

class RakutenPayMailParser {
  constructor(private rawText: string) {}

  extractDate(): string | null {
    const match = this.rawText.match(
      /ご利用日時\s+(\d{4}\/\d{2}\/\d{2}.*?\d{2}:\d{2})/
    );
    return match ? match[1].trim() : null;
  }

  extractAmount(): number | null {
    const match = this.rawText.match(/決済総額\s+([\d,]+)円/);
    return match ? parseInt(match[1].replace(/,/g, ""), 10) : null;
  }

  extractStoreName(): string | null {
    const match = this.rawText.match(/ご利用店舗\s+(.+)/);
    return match ? match[1].trim() : null;
  }

  extractUsedPoint(): number | null {
    const match = this.rawText.match(/ポイント\s+([\d,]+)/);
    return match ? parseInt(match[1].replace(/,/g, ""), 10) : null;
  }

  toExpense(): Expense {
    const a: Expense = {};
    return a;
  }
}
