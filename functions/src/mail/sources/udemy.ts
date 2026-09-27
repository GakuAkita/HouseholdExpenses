import { UdemyMailParser } from "../parsers/UdemyMailParser";
import { assignCategoryById } from "../../domain/categoryAssign";
import { getUdemyMailIds } from "../../infra/gmail/mailQueries";
import { FuncStatus } from "../../type/FuncStatus";
import { UdemySetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { extracted, failed, missingInternalDate, noDataAttached } from "./common";

/** Udemy receipt: one expense per course. The category is set in the settings. */
export const udemySource: MailSource<UdemySetting> = {
  nodeName: "udemy",

  findMailIds: getUdemyMailIds,

  async toExpenses(mail, setting, context) {
    if (!mail.internalDate) return missingInternalDate("Udemy");
    const parsed = new UdemyMailParser(mail.rawText, mail.internalDate).toExpenses();
    if (parsed.status !== FuncStatus.SUCCESS) return failed(parsed);
    if (!parsed.data) return noDataAttached("Udemy");
    return extracted(
      parsed.data.map((expense) => assignCategoryById(expense, setting.categoryId, context.categories))
    );
  },
};
