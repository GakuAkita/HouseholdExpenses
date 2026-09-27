import { readFileSync } from "fs";
import { join } from "path";
import { describe, expect, it } from "vitest";
import { AmazonItemDispatchedMailParser } from "../../src/mail/parsers/AmazonItemDispatchedMailParser";
import { AmazonItemMailParser } from "../../src/mail/parsers/AmazonItemMailParser";
import { AmazonKindleMailParser } from "../../src/mail/parsers/AmazonKindleMailParser";
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
    expect(new RakutenPayMailParser(fixture("rakuten_pay")).toExpense()).toMatchObject({ datetime: "2026-09-15T03:34:00.000Z", amount: 1000, storeName: "ローソン 高松店" });
  });

  it("fails when points exceed the amount", () => {
    const text = fixture("rakuten_pay").replace("￥200", "￥5,000");
    expect(() => new RakutenPayMailParser(text).toExpense()).toThrow(MailParseError);
  });

  it("fails without the store", () => {
    const text = fixture("rakuten_pay").replace("▼ご利用店舗 ローソン 高松店\n", "");
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
