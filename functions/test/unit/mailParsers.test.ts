import { readFileSync } from "fs";
import { join } from "path";
import { describe, expect, it } from "vitest";
import { AmazonItemDispatchedMailParser } from "../../src/mail/parsers/AmazonItemDispatchedMailParser";
import { AmazonItemMailParser } from "../../src/mail/parsers/AmazonItemMailParser";
import { AmazonKindleMailParser } from "../../src/mail/parsers/AmazonKindleMailParser";
import { AmazonSubscribeNextShipmentMailParser } from "../../src/mail/parsers/AmazonSubscribeNextShipmentMailParser";
import { stripHtmlTags } from "../../src/infra/gmail/extractHtmlBody";
import { RakutenCardAllParser } from "../../src/mail/parsers/RakutenCardAllParser";
import { RakutenCardETCParser } from "../../src/mail/parsers/RakutenCardETCParser";
import { RakutenPayMailParser } from "../../src/mail/parsers/RakutenPayMailParser";
import { ShikokuElectricPowerMailParser } from "../../src/mail/parsers/ShikokuElectricPowerMailParser";
import { UdemyMailParser } from "../../src/mail/parsers/UdemyMailParser";
import { MailParseError } from "../../src/mail/parsers/MailParserBase";

/* Synthetic emails. See test/fixtures/mail/README.md. */
const fixture = (name: string) =>
  readFileSync(join(__dirname, "../fixtures/mail", `${name}.txt`), "utf8").replace(/\r\n/g, "\n");

/* Gmail's internalDate: 2026-09-15T03:00:00.000Z */
const internalDate = String(Date.UTC(2026, 8, 15, 3, 0, 0));
const internalDateIso = "2026-09-15T03:00:00.000Z";

describe("RakutenPayMailParser", () => {
  it("extracts the JST time, the store and the amount minus used points", () => {
    expect(new RakutenPayMailParser(fixture("rakuten_pay")).toExpense()).toMatchObject({ datetime: "2026-09-15T03:34:00.000Z", amount: 1000, storeName: "ローソン 駅前店" });
  });

  it("fails when points exceed the amount", () => {
    const text = fixture("rakuten_pay").replace("￥200", "￥5,000");
    expect(() => new RakutenPayMailParser(text).toExpense()).toThrow(MailParseError);
  });

  it("fails without the store", () => {
    const text = fixture("rakuten_pay").replace("▼ご利用店舗 ローソン 駅前店\n", "");
    expect(() => new RakutenPayMailParser(text).toExpense()).toThrow(MailParseError);
  });
});

describe("AmazonKindleMailParser", () => {
  it("extracts the title and the total, and takes the date from internalDate", () => {
    expect(new AmazonKindleMailParser(fixture("amazon_kindle"), internalDate).toExpense()).toMatchObject({ datetime: internalDateIso, amount: 1782, itemName: "リーダブルコード" });
  });

  it("fails without the total", () => {
    const text = fixture("amazon_kindle").replace("総計: ￥ 1,782", "");
    expect(() => new AmazonKindleMailParser(text, internalDate).toExpense()).toThrow(MailParseError);
  });
});

describe("ShikokuElectricPowerMailParser", () => {
  it("extracts the billed amount", () => {
    expect(
      new ShikokuElectricPowerMailParser(fixture("shikoku_electric_power"), internalDate).toExpense()
    ).toMatchObject({ datetime: internalDateIso, amount: 3456 });
  });
});

describe("AmazonItemMailParser", () => {
  it("extracts one expense per product with the line price", () => {
    const result = new AmazonItemMailParser(fixture("amazon_item_with_products"), internalDate).toExpenses();
    expect(result).toEqual([
      { datetime: internalDateIso, amount: 1599, itemName: "ランニング キャップ メンズ" },
      { datetime: internalDateIso, amount: 2400, itemName: "水筒 500ml" },
    ]);
  });

  it("falls back to the order total when products aren't listed", () => {
    const result = new AmazonItemMailParser(fixture("amazon_item_total_only"), internalDate).toExpenses();
    expect(result).toEqual([{ datetime: internalDateIso, amount: 907 }]);
  });

  it("does not read prices with a thousands separator (current behaviour)", () => {
    const text = fixture("amazon_item_with_products").replace("1599 JPY", "1,599 JPY");
    const result = new AmazonItemMailParser(text, internalDate).toExpenses();
    expect(result.map((e) => e.itemName)).toEqual(["水筒 500ml"]);
  });

  it("treats an order total of 0 as no amount (current behaviour)", () => {
    const text = fixture("amazon_item_total_only").replace("注文合計： ￥ 907", "注文合計： ￥ 0");
    expect(new AmazonItemMailParser(text, internalDate).toExpenses()).toEqual([
      { datetime: internalDateIso, amount: undefined },
    ]);
  });

  it("fails when nothing can be extracted", () => {
    expect(() => new AmazonItemMailParser("hello", internalDate).toExpenses()).toThrow(MailParseError);
  });
});

describe("AmazonItemDispatchedMailParser", () => {
  it("splits a line into one expense per unit", () => {
    const result = new AmazonItemDispatchedMailParser(fixture("amazon_dispatched"), internalDate).toExpenses();
    const expense = {
      datetime: internalDateIso,
      amount: 1200,
      itemName: "サントリー 天然水 2L×9本",
      storeName: "Amazon",
    };
    expect(result).toEqual([expense, expense]);
  });
});

describe("UdemyMailParser", () => {
  it("extracts courses from 'List Price / Your Price' lines", () => {
    const result = new UdemyMailParser(fixture("udemy_price_lines"), internalDate).toExpenses();
    expect(result).toEqual([
      { datetime: internalDateIso, amount: 1800, itemName: "Complete Python Bootcamp" },
    ]);
  });

  it("extracts courses from the course table", () => {
    const result = new UdemyMailParser(fixture("udemy_course_table"), internalDate).toExpenses();
    expect(result).toEqual([{ datetime: internalDateIso, amount: 1500, itemName: "Docker Mastery" }]);
  });
});

describe("RakutenCardETCParser", () => {
  it("extracts every ETC block", () => {
    const result = new RakutenCardETCParser(fixture("rakuten_card_etc")).toExpenses();
    expect(result).toEqual([
      { datetime: "2026-09-10T00:00:00.000Z", amount: 1230, itemName: "ＥＴＣカード売上" },
      { datetime: "2026-09-12T00:00:00.000Z", amount: 2000, itemName: "ＥＴＣカード売上" },
    ]);
  });

  /*
   * Known bug: a block's regex starts at the non-ETC "■利用日" and runs lazily until the next ETC
   * "■利用先", so the second expense gets the non-ETC date and amount (500 on 9/11).
   * it.fails passes while the bug exists; make it a normal test when fixing it.
   */
  it.fails("ignores a non-ETC charge between ETC charges", () => {
    const text = fixture("rakuten_card_etc").replace(
      "■利用日: 2026/09/12",
      "■利用日: 2026/09/11\n■利用先: ローソン\n■利用金額: 500 円\n\n■利用日: 2026/09/12"
    );
    expect(new RakutenCardETCParser(text).toExpenses().map((e) => e.amount)).toEqual([1230, 2000]);
  });

  it("fails without ETC blocks", () => {
    expect(() => new RakutenCardETCParser("楽天カードご利用のお知らせ").toExpenses()).toThrow(MailParseError);
  });
});

describe("RakutenCardAllParser", () => {
  it("extracts every charge with its store, ETC included", () => {
    expect(new RakutenCardAllParser(fixture("rakuten_card_all")).toExpenses()).toEqual([
      { datetime: "2026-03-10T00:00:00.000Z", amount: 1230, storeName: "ローソン 駅前店" },
      { datetime: "2026-03-12T00:00:00.000Z", amount: 2000, storeName: "ＥＴＣカード売上" },
    ]);
  });

  it("reads the HTML layout as Gmail renders it, without 合計 rows", () => {
    expect(new RakutenCardAllParser(fixture("rakuten_card_all_html")).toExpenses()).toEqual([
      { datetime: "2026-03-05T00:00:00.000Z", amount: 1100, storeName: "ｺﾝﾋﾞﾆ ｴｷﾏｴﾃﾝ" },
      { datetime: "2026-03-06T00:00:00.000Z", amount: 860, storeName: "ｽ-ﾊﾟ-" },
      { datetime: "2026-03-11T00:00:00.000Z", amount: 2500, storeName: "ｹｲﾀｲﾂｳｼﾝﾘﾖｳ" },
    ]);
  });

  it("reads the HTML layout with the tags removed", () => {
    /* Read as is: this fixture keeps CRLF, like stripped HTML from a real mail. */
    const text = readFileSync(join(__dirname, "../fixtures/mail/rakuten_card_all_html_stripped.txt"), "utf8");
    expect(new RakutenCardAllParser(text).toExpenses()).toEqual([
      { datetime: "2026-03-08T00:00:00.000Z", amount: 640, storeName: "ﾄﾞﾗﾂｸﾞｽﾄｱ" },
      { datetime: "2026-03-11T00:00:00.000Z", amount: 2500, storeName: "ｹｲﾀｲﾂｳｼﾝﾘﾖｳ" },
    ]);
  });

  it("skips 楽天ペイ残高 チャージ", () => {
    const charge = (storeName: string) =>
      fixture("rakuten_card_all").replace("ローソン 駅前店", storeName);
    expect(new RakutenCardAllParser(charge("楽天ペイ残高　チャージ")).toExpenses().map((e) => e.amount)).toEqual([2000]);
    expect(new RakutenCardAllParser(charge("楽天ペイ残高 チャージ")).toExpenses().map((e) => e.amount)).toEqual([2000]);
    expect(new RakutenCardAllParser(charge("楽天ペイ残高チャージ")).toExpenses().map((e) => e.amount)).toEqual([2000]);
  });

  it("returns nothing for a mail with only a 楽天ペイ残高 チャージ", () => {
    const text = fixture("rakuten_card_all")
      .replace("ローソン 駅前店", "楽天ペイ残高　チャージ")
      .replace("ＥＴＣカード売上", "楽天ペイ残高　チャージ");
    expect(new RakutenCardAllParser(text).toExpenses()).toEqual([]);
  });

  it("fails when a charge has no amount", () => {
    const text = fixture("rakuten_card_all").replace("■利用金額: 2,000 円", "");
    expect(() => new RakutenCardAllParser(text).toExpenses()).toThrow(MailParseError);
  });

  it("fails without charges", () => {
    expect(() => new RakutenCardAllParser("楽天カードご利用のお知らせ").toExpenses()).toThrow(MailParseError);
  });
});

describe("AmazonSubscribeNextShipmentMailParser", () => {
  /* The job reads the HTML part with the tags removed. Kept with the fixture's line endings (CRLF, like real mails) and with LF. */
  const html = readFileSync(join(__dirname, "../fixtures/mail", "amazon_subscribe_price_changed.html"), "utf8");

  it.each([
    ["CRLF", html],
    ["LF", html.replace(/\r\n/g, "\n")],
  ])("reads the new price and the quantity of each item when lines are separated by blank lines (%s)", (_, mail) => {
    expect(new AmazonSubscribeNextShipmentMailParser(stripHtmlTags(mail)).toSubscribeItem()).toEqual([
      { productName: "サンプル 天然水 ラベルレス 500ml ×24本", price: 1401, quantity: 1 },
      { productName: "サンプル キッチンスポンジ 抗菌 グリーン", price: 2830, quantity: 12 },
    ]);
  });

  it("reads the price without 新価格", () => {
    const text = stripHtmlTags(readFileSync(join(__dirname, "../fixtures/mail", "amazon_subscribe_next_shipment.html"), "utf8"));
    expect(new AmazonSubscribeNextShipmentMailParser(text).toSubscribeItem()).toEqual([
      { productName: "by Amazon 天然水 ラベルレス 500ml ×24本 富士山の天然水 バナジウム含有 水", price: 1126, quantity: 1 },
    ]);
  });

  it("uses 1 as the price when the item has none", () => {
    const text = "商品A\n\n2 ごとのユニット 1 月\n\n配送を管理する";
    expect(new AmazonSubscribeNextShipmentMailParser(text).toSubscribeItem()).toEqual([
      { productName: "商品A", price: 1, quantity: 2 },
    ]);
  });

  it("fails when the mail has no items", () => {
    expect(() => new AmazonSubscribeNextShipmentMailParser("注文合計 (税込) ￥1,189").toSubscribeItem()).toThrow(MailParseError);
  });
});
