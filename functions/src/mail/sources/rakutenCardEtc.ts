import { assignCategoryById } from "../../domain/categoryAssign";
import { getRakutenCardETCMailIds } from "../../infra/gmail/mailQueries";
import { RakutenCardETCSetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { RakutenCardETCParser } from "../parsers/RakutenCardETCParser";

/** 楽天カード ETC: one expense per ETC charge, dated by the charge. The category is set in the settings. */
export const rakutenCardEtcSource: MailSource<RakutenCardETCSetting> = {
  nodeName: "rakuten_card_etc",

  findMailIds: getRakutenCardETCMailIds,

  async toExpenses(mail, setting, context) {
    return new RakutenCardETCParser(mail.rawText)
      .toExpenses()
      .map((expense) => assignCategoryById(expense, setting.categoryId, context.categories));
  },
};
