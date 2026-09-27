import * as admin from "firebase-admin";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { AmazonSubscribeMonitorItemsProcessor } from "../../src/jobs/AmazonSubscribeMonitorItemsProcessor";
import { fixedClock } from "../../src/shared/clock";
import { AmazonMailSubjects } from "../../src/type/AmazonMailSubjects";
import { createAmazonSubscribeSettingInstance, EmailProvider } from "../../src/type/Mailbox";
import { createTestServices, deleteTestUser, newTestUserId, testGmailOf, testSecrets } from "./emulator";
import { createFakeGmail, FakeMail, mailFixture } from "./fakeGmail";

const now = new Date("2026-09-15T05:00:00.000Z");
const amazon = "no-reply@amazon.co.jp";

const mails: FakeMail[] = [
  {
    id: "next-shipment-1",
    from: amazon,
    subject: AmazonMailSubjects.NEXT_SHIPMENT,
    internalDate: new Date("2026-09-14T01:00:00.000Z"),
    text: "次回の定期おトク便の自動配信が間近に迫っています。",
    html: mailFixture("amazon_subscribe_next_shipment", "html"),
  },
  {
    id: "cancel-1",
    from: amazon,
    subject: AmazonMailSubjects.CANCELED_SUBSCRIPTION,
    internalDate: new Date("2026-09-14T02:00:00.000Z"),
    text: mailFixture("amazon_subscribe_cancel"),
  },
];

describe("Amazon subscribe monitor", () => {
  const userId = newTestUserId();
  const gmail = testGmailOf(userId);
  const fakeGmail = createFakeGmail(mails);
  const services = createTestServices({ clock: fixedClock(now), createGmailClient: fakeGmail.factory });
  const processor = new AmazonSubscribeMonitorItemsProcessor(
    userId,
    services.mailboxExtractionService,
    services.runtime
  );

  const monitor = async () =>
    (await admin.database().ref(`users/${userId}/mailbox_extraction/amazon_subscribe_monitor`).get()).val();

  beforeEach(async () => {
    await admin.auth().createUser({ uid: userId, email: gmail });
    await services.mailboxExtractionService.saveGmailToken(
      userId,
      { refreshToken: "raw-refresh-token", gmail },
      testSecrets.encryptionKey
    );
    await services.mailboxExtractionService.setMailTypeSetting(
      userId,
      createAmazonSubscribeSettingInstance({ enabled: true, emailProvider: EmailProvider.GMAIL })
    );
  });

  afterEach(() => deleteTestUser(userId));

  it("adds items from next-shipment mails and removes cancelled ones", async () => {
    await services.mailboxExtractionService.addAmazonSubscribeItem(userId, {
      productName: "サントリー 天然水 2L×9本",
      price: 1000,
      quantity: 1,
    });

    await processor.handleAmazonSubscribeItems();

    const { subscribe_items: items, last_exec: lastExec } = await monitor();
    expect(Object.values(items)).toEqual([
      expect.objectContaining({
        productName: "by Amazon 天然水 ラベルレス 500ml ×24本 富士山の天然水 バナジウム含有 水",
        price: 1126,
        quantity: 1,
        timestamp: now.getTime(),
      }),
    ]);
    expect(lastExec).toEqual({ timestamp: now.getTime(), lastMsgId: "cancel-1" });
  });

  it("updates the price of an item that is already registered", async () => {
    await services.mailboxExtractionService.addAmazonSubscribeItem(userId, {
      productName: "by Amazon 天然水 ラベルレス 500ml ×24本",
      price: 1000,
      quantity: 1,
      enabled: false,
    });

    await processor.handleAmazonSubscribeItems();

    const { subscribe_items: items } = await monitor();
    expect(Object.values(items)).toEqual([
      expect.objectContaining({
        productName: "by Amazon 天然水 ラベルレス 500ml ×24本",
        price: 1126,
        enabled: true,
      }),
    ]);
  });

  it("does nothing when the user hasn't enabled the monitor", async () => {
    await admin.database().ref(`users/${userId}/mailbox_extraction/email_template_settings`).remove();
    const queriesBefore = fakeGmail.queries.length;

    await processor.handleAmazonSubscribeItems();

    expect(fakeGmail.queries).toHaveLength(queriesBefore);
  });
});
