import { ShikokuElectricPowerMailParser } from "../../myFunc/Parser/ShikokuElectricPowerMailParser";
import { assignCategoryById } from "../../myFunc/utility/cateogryAssign";
import { getShikokuElectricMailIds } from "../../myFunc/utility/gmail/mailQueries";
import { FuncStatus } from "../../type/FuncStatus";
import { ShikokuElectricPowerSetting } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { extracted, failed, missingInternalDate } from "./common";

/** 四国電力: one bill per mail, dated by the mail. The category is set in the settings. */
export const shikokuElectricPowerSource: MailSource<ShikokuElectricPowerSetting> = {
  nodeName: "shikoku_electric_power",

  findMailIds: getShikokuElectricMailIds,

  async toExpenses(mail, setting, context) {
    if (!mail.internalDate) return missingInternalDate("ShikokuElectricPower");
    const parsed = new ShikokuElectricPowerMailParser(mail.rawText, mail.internalDate).toExpense();
    if (parsed.status !== FuncStatus.SUCCESS || !parsed.data) return failed(parsed);
    return extracted([assignCategoryById(parsed.data, setting.categoryId, context.categories)]);
  },
};
