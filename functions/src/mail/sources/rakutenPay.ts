import { getRakutenPayMailIds } from "../../infra/gmail/mailQueries";
import { RakutenPaySetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { RakutenPayMailParser } from "../parsers/RakutenPayMailParser";
import { assignByStoreName } from "./common";

/** 楽天Pay: one payment per mail. The category comes from the store name rules. */
export const rakutenPaySource: MailSource<RakutenPaySetting> = {
  nodeName: "rakuten_pay",

  findMailIds: getRakutenPayMailIds,

  async toExpenses(mail, _setting, context) {
    const expense = new RakutenPayMailParser(mail.rawText).toExpense();
    return [assignByStoreName(expense, context)];
  },
};
