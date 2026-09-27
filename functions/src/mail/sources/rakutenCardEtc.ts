import { RakutenCardETCParser } from "../parsers/RakutenCardETCParser";
import { assignCategoryById } from "../../domain/categoryAssign";
import { getRakutenCardETCMailIds } from "../../infra/gmail/mailQueries";
import { FuncStatus } from "../../type/FuncStatus";
import { RakutenCardETCSetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { extracted, failed, noDataAttached } from "./common";

/** 楽天カード ETC: one expense per ETC charge, dated by the charge. The category is set in the settings. */
export const rakutenCardEtcSource: MailSource<RakutenCardETCSetting> = {
  nodeName: "rakuten_card_etc",

  findMailIds: getRakutenCardETCMailIds,

  async toExpenses(mail, setting, context) {
    const parsed = new RakutenCardETCParser(mail.rawText).toExpenses();
    if (parsed.status !== FuncStatus.SUCCESS) return failed(parsed);
    if (!parsed.data) return noDataAttached("RakutenCardETC");
    return extracted(
      parsed.data.map((expense) => assignCategoryById(expense, setting.categoryId, context.categories))
    );
  },
};
