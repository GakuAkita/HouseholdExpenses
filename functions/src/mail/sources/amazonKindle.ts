import { assignCategoryById } from "../../domain/categoryAssign";
import { getAmazonKindleMailIds } from "../../infra/gmail/mailQueries";
import { AmazonKindleSetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { AmazonKindleMailParser } from "../parsers/AmazonKindleMailParser";
import { requireInternalDate } from "./common";

/** Amazon Kindle: one book per mail, dated by the mail. The category is set in the settings. */
export const amazonKindleSource: MailSource<AmazonKindleSetting> = {
  nodeName: "amazon_kindle",

  findMailIds: getAmazonKindleMailIds,

  async toExpenses(mail, setting, context) {
    const internalDate = requireInternalDate(mail.internalDate, "AmazonKindle");
    const expense = new AmazonKindleMailParser(mail.rawText, internalDate).toExpense();
    return [assignCategoryById(expense, setting.categoryId, context.categories)];
  },
};
