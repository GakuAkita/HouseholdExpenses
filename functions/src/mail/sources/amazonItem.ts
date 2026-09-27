import { AmazonItemMailParser } from "../../myFunc/Parser/AmazonItemMailParser";
import { getAmazonItemMailIds } from "../../myFunc/utility/gmail/mailQueries";
import { FuncStatus } from "../../type/FuncStatus";
import { AmazonItemSetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { assignByProductName, extracted, failed, missingInternalDate, noDataAttached } from "./common";

/** Amazon order confirmation: one expense per product. The category comes from the product name rules. */
export const amazonItemSource: MailSource<AmazonItemSetting> = {
  nodeName: "amazon_item",

  findMailIds: getAmazonItemMailIds,

  async toExpenses(mail, _setting, context) {
    if (!mail.internalDate) return missingInternalDate("AmazonItem");
    const parsed = new AmazonItemMailParser(mail.rawText, mail.internalDate).toExpenses();
    if (parsed.status !== FuncStatus.SUCCESS) return failed(parsed);
    if (!parsed.data) return noDataAttached("AmazonItem");
    return extracted(parsed.data.map((expense) => assignByProductName(expense, context)));
  },
};
