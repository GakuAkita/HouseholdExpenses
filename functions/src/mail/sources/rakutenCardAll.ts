import { isExcludedStore } from "../../domain/rakutenCardExclude";
import { getRakutenCardAllMailIds } from "../../infra/gmail/mailQueries";
import { RakutenCardAllSetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { RakutenCardAllParser } from "../parsers/RakutenCardAllParser";
import { assignByStoreName } from "./common";

/** 楽天カード (all charges): one expense per charge. The category comes from the store name rules. */
export const rakutenCardAllSource: MailSource<RakutenCardAllSetting> = {
  nodeName: "rakuten_all",

  /* Users who chose HTML mails in their Rakuten settings get the HTML layout. */
  readsHtmlWithoutText: true,

  findMailIds: getRakutenCardAllMailIds,

  async toExpenses(mail, _setting, context) {
    const expenses = new RakutenCardAllParser(mail.rawText).toExpenses();
    const excludeItems = await context.loadRakutenCardExcludeItems();
    return expenses
      .filter((expense) => !expense.storeName || !isExcludedStore(expense.storeName, excludeItems))
      .map((expense) => assignByStoreName(expense, context));
  },
};
