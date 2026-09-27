import { readFileSync } from "fs";
import { join } from "path";
import { describe, expect, it, vi } from "vitest";
import { AssignmentCondition } from "../../src/constants/AssignmentCondition";
import { ExtractionContext, MailSource } from "../../src/mail/mailSource";
import { mailSourceFor } from "../../src/mail/sources";
import { GmailClient } from "../../src/myFunc/Client/GmailApiClient";
import { Category } from "../../src/type/Category";
import { FuncStatus } from "../../src/type/FuncStatus";
import {
  AllMailType,
  AmazonSubscribeItem,
  createAmazonItemSettingInstance,
  createAmazonKindleSettingInstance,
  createAmazonSubscribeSettingInstance,
  createRakutenCardETCSettingInstance,
  createRakutenPaySettingInstance,
  createShikokuElectricPowerSettingInstance,
  createUdemySettingInstance,
  EmailProvider,
  mailboxExtractionSchedules,
} from "../../src/type/Mailbox";

const fixture = (name: string) =>
  readFileSync(join(__dirname, "../fixtures/mail", `${name}.txt`), "utf8").replace(/\r\n/g, "\n");

const internalDate = String(Date.UTC(2026, 8, 15, 3, 0, 0));
const internalDateIso = "2026-09-15T03:00:00.000Z";

const food: Category = { id: "food", timestamp: 1, name: "食費", enabled: true };
const daily: Category = { id: "daily", timestamp: 1, name: "日用品", enabled: true };
const bills: Category = { id: "bills", timestamp: 1, name: "光熱費", enabled: true };

const context = (subscribeItems?: Record<string, AmazonSubscribeItem>): ExtractionContext => ({
  categories: { food, daily, bills },
  categoryAssignmentData: {
    storeName: {
      a: { name: "ローソン 高松店", categoryId: "food", condition: AssignmentCondition.EXACT_MATCH, regex: false, generatedTyep: null },
    },
    productName: {
      b: { name: "水", categoryId: "daily", condition: AssignmentCondition.CONTAINS, regex: false, generatedTyep: null },
    },
  },
  loadEnabledAmazonSubscribeItems: async () =>
    subscribeItems
      ? { status: FuncStatus.SUCCESS, data: subscribeItems }
      : { status: FuncStatus.EMPTY },
});

const gmail = { enabled: true, emailProvider: EmailProvider.GMAIL };

const sourceOf = <S extends AllMailType>(setting: S): MailSource<S> => {
  const source = mailSourceFor(setting);
  if (!source) throw new Error(`No source for ${setting.nodeName}`);
  return source;
};

describe("mail source registry", () => {
  it("has a source for every scheduled mail type", () => {
    for (const schedule of mailboxExtractionSchedules) {
      for (const type of schedule.mailTypes) {
        expect(mailSourceFor(type)?.nodeName).toBe(type.nodeName);
      }
    }
  });

  it("returns undefined for an unknown node name", () => {
    expect(mailSourceFor({ nodeName: "unknown" } as unknown as AllMailType)).toBeUndefined();
  });

  it("each source queries its own sender", async () => {
    const senders: Record<string, string> = {
      rakuten_pay: "no-reply@pay.rakuten.co.jp",
      amazon_kindle: "digital-no-reply@amazon.co.jp",
      shikoku_electric_power: "yonden-con@yonden.co.jp",
      amazon_item: "auto-confirm@amazon.co.jp",
      amazon_subscribe: "shipment-tracking@amazon.co.jp",
      udemy: "hello@alerts.udemy.com",
      rakuten_card_etc: "info@mail.rakuten-card.co.jp",
    };
    for (const schedule of mailboxExtractionSchedules) {
      for (const type of schedule.mailTypes) {
        const queryMessages = vi.fn().mockResolvedValue({ status: FuncStatus.SUCCESS, data: [] });
        await sourceOf(type).findMailIds({ queryMessages } as unknown as GmailClient, 100, 200);
        expect(queryMessages.mock.calls[0][0]).toContain(`from:${senders[type.nodeName]}`);
      }
    }
  });
});

describe("mail sources", () => {
  it("rakuten_pay assigns the category by store name", async () => {
    const setting = createRakutenPaySettingInstance(gmail);
    const result = await sourceOf(setting).toExpenses({ rawText: fixture("rakuten_pay") }, setting, context());
    expect(result.data).toEqual([
      { datetime: "2026-09-15T03:34:00.000Z", amount: 1000, storeName: "ローソン 高松店", category: food },
    ]);
  });

  it("amazon_kindle and shikoku_electric_power use the category in the setting", async () => {
    const kindle = createAmazonKindleSettingInstance({ ...gmail, categoryId: "daily" });
    expect(
      (await sourceOf(kindle).toExpenses({ rawText: fixture("amazon_kindle"), internalDate }, kindle, context())).data
    ).toEqual([{ datetime: internalDateIso, amount: 1782, itemName: "リーダブルコード", category: daily }]);

    const shikoku = createShikokuElectricPowerSettingInstance({ ...gmail, categoryId: "bills" });
    expect(
      (await sourceOf(shikoku).toExpenses({ rawText: fixture("shikoku_electric_power"), internalDate }, shikoku, context()))
        .data
    ).toEqual([{ datetime: internalDateIso, amount: 3456, category: bills }]);
  });

  it("fails without internalDate for mails dated by Gmail", async () => {
    const kindle = createAmazonKindleSettingInstance(gmail);
    const result = await sourceOf(kindle).toExpenses({ rawText: fixture("amazon_kindle") }, kindle, context());
    expect(result.status).toBe(FuncStatus.ERROR);
  });

  it("amazon_item assigns categories by product name", async () => {
    const setting = createAmazonItemSettingInstance(gmail);
    const result = await sourceOf(setting).toExpenses(
      { rawText: fixture("amazon_item_with_products"), internalDate },
      setting,
      context()
    );
    expect(result.data?.map((e) => [e.itemName, e.category?.id])).toEqual([
      ["ランニング キャップ メンズ", undefined],
      ["水筒 500ml", "daily"],
    ]);
  });

  it("amazon_subscribe keeps only products in the subscribe list", async () => {
    const setting = createAmazonSubscribeSettingInstance(gmail);
    const mail = { rawText: fixture("amazon_dispatched"), internalDate };

    const subscribed = await sourceOf(setting).toExpenses(
      mail,
      setting,
      context({ item1: { id: "item1", productName: "サントリー 天然水" } })
    );
    expect(subscribed.data).toHaveLength(2);
    expect(subscribed.data?.[0]).toMatchObject({ amount: 1200, storeName: "Amazon", category: daily });

    const other = await sourceOf(setting).toExpenses(
      mail,
      setting,
      context({ item1: { id: "item1", productName: "コカ・コーラ" } })
    );
    expect(other).toEqual({ status: FuncStatus.SUCCESS, data: [] });

    const noList = await sourceOf(setting).toExpenses(mail, setting, context());
    expect(noList).toEqual({ status: FuncStatus.SUCCESS, data: [] });
  });

  it("udemy and rakuten_card_etc use the category in the setting for every expense", async () => {
    const udemy = createUdemySettingInstance({ ...gmail, categoryId: "daily" });
    const courses = await sourceOf(udemy).toExpenses(
      { rawText: fixture("udemy_price_lines"), internalDate },
      udemy,
      context()
    );
    expect(courses.data).toEqual([
      { datetime: internalDateIso, amount: 1800, itemName: "Complete Python Bootcamp", category: daily },
    ]);

    const etc = createRakutenCardETCSettingInstance({ ...gmail, categoryId: "bills" });
    const charges = await sourceOf(etc).toExpenses({ rawText: fixture("rakuten_card_etc") }, etc, context());
    expect(charges.data?.map((e) => [e.amount, e.category?.id])).toEqual([
      [1230, "bills"],
      [2000, "bills"],
    ]);
  });

  it("passes on the parser's error", async () => {
    const setting = createRakutenPaySettingInstance(gmail);
    const result = await sourceOf(setting).toExpenses({ rawText: "hello" }, setting, context());
    expect(result.status).toBe(FuncStatus.ERROR);
    expect(result.data).toBeUndefined();
  });
});
