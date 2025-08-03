import { Expense } from "../../type/Expense";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";

export class RakutenCardETCParser {
  constructor(private rawText: string) {}

  extractDate() {}

  toExpenses(): FuncResultWithData<Expense[]> {
    console.log("------------------------------------\n");
    console.log(`${this.rawText}`);

    const pattern =
      /■利用日:\s*(\d{4}\/\d{2}\/\d{2})\s*■利用先:\s*(.+?)\s*■利用者:.*?■支払方法:.*?■利用金額:\s*(\d+)\s*円/g;

    return {
      status: FuncStatus.ERROR,
      message: "No expense was extracted from RakutenETC",
    };
  }
}
