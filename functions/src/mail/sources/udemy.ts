import { assignCategoryById } from "../../domain/categoryAssign";
import { getUdemyMailIds } from "../../infra/gmail/mailQueries";
import { UdemySetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { UdemyMailParser } from "../parsers/UdemyMailParser";
import { requireInternalDate } from "./common";

/** Udemy receipt: one expense per course. The category is set in the settings. */
export const udemySource: MailSource<UdemySetting> = {
  nodeName: "udemy",

  findMailIds: getUdemyMailIds,

  async toExpenses(mail, setting, context) {
    const internalDate = requireInternalDate(mail.internalDate, "Udemy");
    return new UdemyMailParser(mail.rawText, internalDate)
      .toExpenses()
      .map((expense) => assignCategoryById(expense, setting.categoryId, context.categories));
  },
};
