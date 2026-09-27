import { getAmazonItemMailIds } from "../../infra/gmail/mailQueries";
import { AmazonItemSetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { AmazonItemMailParser } from "../parsers/AmazonItemMailParser";
import { assignByProductName, requireInternalDate } from "./common";

/** Amazon order confirmation: one expense per product. The category comes from the product name rules. */
export const amazonItemSource: MailSource<AmazonItemSetting> = {
  nodeName: "amazon_item",

  findMailIds: getAmazonItemMailIds,

  async toExpenses(mail, _setting, context) {
    const internalDate = requireInternalDate(mail.internalDate, "AmazonItem");
    return new AmazonItemMailParser(mail.rawText, internalDate)
      .toExpenses()
      .map((expense) => assignByProductName(expense, context));
  },
};
