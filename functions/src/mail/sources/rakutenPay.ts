import { RakutenPayMailParser } from "../../myFunc/Parser/RakutenPayMailParser";
import { getRakutenPayMailIds } from "../../myFunc/utility/gmail/mailQueries";
import { FuncStatus } from "../../type/FuncStatus";
import { RakutenPaySetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { assignByStoreName, extracted, failed } from "./common";

/** 楽天Pay: one payment per mail. The category comes from the store name rules. */
export const rakutenPaySource: MailSource<RakutenPaySetting> = {
  nodeName: "rakuten_pay",

  findMailIds: getRakutenPayMailIds,

  async toExpenses(mail, _setting, context) {
    const parsed = new RakutenPayMailParser(mail.rawText).toExpense();
    if (parsed.status !== FuncStatus.SUCCESS || !parsed.data) return failed(parsed);
    return extracted([assignByStoreName(parsed.data, context)]);
  },
};
