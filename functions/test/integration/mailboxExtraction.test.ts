import * as admin from "firebase-admin";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { AssignmentCondition } from "../../src/constants/AssignmentCondition";
import { MailboxExtractionProcessor } from "../../src/myFunc/Processor/MailboxExtractionProcessor";
import { fixedClock } from "../../src/shared/clock";
import { Category } from "../../src/type/Category";
import {
  AllMailType,
  createAmazonItemSettingInstance,
  createAmazonKindleSettingInstance,
  createAmazonSubscribeSettingInstance,
  createRakutenPaySettingInstance,
  EmailProvider,
} from "../../src/type/Mailbox";
import { createTestServices, deleteTestUser, newTestUserId, testGmailOf, testSecrets } from "./emulator";
import { createFakeGmail, FakeMail, mailFixture } from "./fakeGmail";

const now = new Date("2026-09-15T05:00:00.000Z");
const mailDate = new Date("2026-09-15T03:00:00.000Z");

const food: Category = { id: "food", timestamp: 1, name: "食費", enabled: true };
const books: Category = { id: "books", timestamp: 1, name: "本", enabled: true };
const daily: Category = { id: "daily", timestamp: 1, name: "日用品", enabled: true };

const mails: FakeMail[] = [
  { id: "rakuten-1", from: "no-reply@pay.rakuten.co.jp", internalDate: mailDate, text: mailFixture("rakuten_pay") },
  { id: "kindle-1", from: "digital-no-reply@amazon.co.jp", internalDate: mailDate, text: mailFixture("amazon_kindle") },
  {
    id: "amazon-1",
    from: "auto-confirm@amazon.co.jp",
    internalDate: mailDate,
    text: mailFixture("amazon_item_with_products"),
  },
  {
    id: "dispatched-1",
    from: "shipment-tracking@amazon.co.jp",
    internalDate: mailDate,
    text: mailFixture("amazon_dispatched"),
  },
];

const gmailSettings: AllMailType[] = [
  createRakutenPaySettingInstance({ enabled: true, emailProvider: EmailProvider.GMAIL }),
  createAmazonKindleSettingInstance({ enabled: true, emailProvider: EmailProvider.GMAIL, categoryId: "books" }),
  createAmazonItemSettingInstance({ enabled: true, emailProvider: EmailProvider.GMAIL }),
  createAmazonSubscribeSettingInstance({ enabled: true, emailProvider: EmailProvider.GMAIL }),
];

describe("mailbox extraction", () => {
  const userId = newTestUserId();
  const gmail = testGmailOf(userId);
  const fakeGmail = createFakeGmail(mails);
  const services = createTestServices({ clock: fixedClock(now), createGmailClient: fakeGmail.factory });
  const processor = new MailboxExtractionProcessor(
    userId,
    services.mailboxExtractionService,
    services.expenseService,
    services.categoryService,
    services.categoryAssignmentService,
    services.runtime
  );

  const expenses = async () =>
    (await admin.firestore().collection(`users/${userId}/expenses`).get()).docs.map((doc) => doc.data());

  const connectGmail = async () => {
    await admin.auth().createUser({ uid: userId, email: gmail });
    await services.mailboxExtractionService.setMailboxExtractionTokenWithEncryption(
      userId,
      { refreshToken: "raw-refresh-token", gmail },
      testSecrets.encryptionKey
    );
  };

  beforeEach(async () => {
    for (const category of [food, books, daily]) {
      await services.categoryService.setCategory(userId, category);
    }
    await admin
      .database()
      .ref(`users/${userId}/category_assignment_data`)
      .set({
        storeName: {
          a: { name: "ローソン 高松店", categoryId: "food", condition: AssignmentCondition.EXACT_MATCH, regex: false },
        },
        productName: {
          b: { name: "水筒", categoryId: "daily", condition: AssignmentCondition.CONTAINS, regex: false },
        },
      });
    for (const setting of gmailSettings) {
      await services.mailboxExtractionService.setMailboxExtractionMailTypeSetting(userId, setting);
    }
    await services.mailboxExtractionService.addAmazonSubscribeMonitorItem(userId, {
      productName: "サントリー 天然水",
    });
  });

  afterEach(() => deleteTestUser(userId));

  it("saves an expense for each mail, with categories, that the app can read", async () => {
    await connectGmail();

    await processor.processAllMailTypeList(gmailSettings);

    /* The Gmail client is created with the decrypted refresh token. */
    expect(fakeGmail.createdWith[0]).toEqual({
      clientId: testSecrets.clientId,
      clientSecret: testSecrets.clientSecret,
      refreshToken: "raw-refresh-token",
    });

    const common = { timestamp: now.getTime() };
    const saved = await expenses();
    for (const expense of saved) {
      /* The app requires id, datetime, timestamp, amount and generatedType. */
      expect(expense).toEqual(
        expect.objectContaining({
          id: expect.any(String),
          datetime: expect.any(String),
          timestamp: expect.any(Number),
          amount: expect.any(Number),
          generatedType: expect.any(String),
        })
      );
    }
    const byType = (type: string) =>
      saved
        .filter((e) => e.generatedType === `mailbox_extraction___${type}`)
        .map(({ id: _, ...rest }) => rest);

    expect(byType("rakuten_pay")).toEqual([
      {
        ...common,
        datetime: "2026-09-15T03:34:00.000Z",
        amount: 1000,
        storeName: "ローソン 高松店",
        category: food,
        generatedType: "mailbox_extraction___rakuten_pay",
      },
    ]);
    expect(byType("amazon_kindle")).toEqual([
      {
        ...common,
        datetime: mailDate.toISOString(),
        amount: 1782,
        itemName: "リーダブルコード",
        category: books,
        generatedType: "mailbox_extraction___amazon_kindle",
      },
    ]);
    expect(byType("amazon_item")).toEqual(
      expect.arrayContaining([
        {
          ...common,
          datetime: mailDate.toISOString(),
          amount: 1599,
          itemName: "ランニング キャップ メンズ",
          category: null,
          generatedType: "mailbox_extraction___amazon_item",
        },
        {
          ...common,
          datetime: mailDate.toISOString(),
          amount: 2400,
          itemName: "水筒 500ml",
          category: daily,
          generatedType: "mailbox_extraction___amazon_item",
        },
      ])
    );
    /* A dispatched item in the subscribe list: one expense per unit. */
    expect(byType("amazon_subscribe")).toHaveLength(2);
    expect(byType("amazon_subscribe")[0]).toMatchObject({
      amount: 1200,
      itemName: "サントリー 天然水 2L×9本",
      storeName: "Amazon",
    });
    expect(saved).toHaveLength(6);

    const lastExec = (await admin.database().ref(`users/${userId}/mailbox_extraction/last_exec`).get()).val();
    expect(lastExec.rakuten_pay).toEqual({ timestamp: now.getTime(), lastMsgId: "rakuten-1" });
  });

  it("doesn't save the same mail twice", async () => {
    await connectGmail();

    await processor.processAllMailTypeList(gmailSettings);
    await processor.processAllMailTypeList(gmailSettings);

    expect(await expenses()).toHaveLength(6);
  });

  it("skips a disabled mail type", async () => {
    await connectGmail();
    const disabled = createRakutenPaySettingInstance({ enabled: false, emailProvider: EmailProvider.GMAIL });
    await services.mailboxExtractionService.setMailboxExtractionMailTypeSetting(userId, disabled);

    await processor.processAllMailTypeList([disabled]);

    expect(await expenses()).toEqual([]);
  });

  it("does nothing until the user connects Gmail", async () => {
    await admin.auth().createUser({ uid: userId, email: gmail });

    await processor.processAllMailTypeList(gmailSettings);

    expect(await expenses()).toEqual([]);
  });
});
