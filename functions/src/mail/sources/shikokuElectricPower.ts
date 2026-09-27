import { assignCategoryById } from "../../domain/categoryAssign";
import { getShikokuElectricMailIds } from "../../infra/gmail/mailQueries";
import { ShikokuElectricPowerSetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { ShikokuElectricPowerMailParser } from "../parsers/ShikokuElectricPowerMailParser";
import { requireInternalDate } from "./common";

/** 四国電力: one bill per mail, dated by the mail. The category is set in the settings. */
export const shikokuElectricPowerSource: MailSource<ShikokuElectricPowerSetting> = {
  nodeName: "shikoku_electric_power",

  findMailIds: getShikokuElectricMailIds,

  async toExpenses(mail, setting, context) {
    const internalDate = requireInternalDate(mail.internalDate, "ShikokuElectricPower");
    const expense = new ShikokuElectricPowerMailParser(mail.rawText, internalDate).toExpense();
    return [assignCategoryById(expense, setting.categoryId, context.categories)];
  },
};
