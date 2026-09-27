import { AmazonKindleMailParser } from "../../myFunc/Parser/AmazonKindleMailParser";
import { assignCategoryById } from "../../myFunc/utility/cateogryAssign";
import { getAmazonKindleMailIds } from "../../myFunc/utility/gmail/mailQueries";
import { FuncStatus } from "../../type/FuncStatus";
import { AmazonKindleSetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { extracted, failed, missingInternalDate } from "./common";

/** Amazon Kindle: one book per mail, dated by the mail. The category is set in the settings. */
export const amazonKindleSource: MailSource<AmazonKindleSetting> = {
  nodeName: "amazon_kindle",

  findMailIds: getAmazonKindleMailIds,

  async toExpenses(mail, setting, context) {
    if (!mail.internalDate) return missingInternalDate("AmazonKindle");
    const parsed = new AmazonKindleMailParser(mail.rawText, mail.internalDate).toExpense();
    if (parsed.status !== FuncStatus.SUCCESS || !parsed.data) return failed(parsed);
    return extracted([assignCategoryById(parsed.data, setting.categoryId, context.categories)]);
  },
};
