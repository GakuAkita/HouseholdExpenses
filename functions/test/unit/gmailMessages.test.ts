import { gmail_v1 } from "googleapis";
import { describe, expect, it } from "vitest";
import { extractTextBody, stripHtmlTags } from "../../src/infra/gmail/extractHtmlBody";
import { filterMessages } from "../../src/infra/gmail/filterMessages";
import { sortGmailMessagesByDate } from "../../src/infra/gmail/getInternalDate";

const message = (internalDate?: string): gmail_v1.Schema$Message => ({ internalDate });
const base64url = (text: string) => Buffer.from(text, "utf8").toString("base64url");

describe("sortGmailMessagesByDate", () => {
  it("sorts newest first and puts messages without a date last", () => {
    const sorted = sortGmailMessagesByDate({
      old: message("1000"),
      none: message(undefined),
      new: message("3000"),
      middle: message("2000"),
    });
    expect(sorted.map(([id]) => id)).toEqual(["new", "middle", "old", "none"]);
  });

  it("can sort oldest first", () => {
    const sorted = sortGmailMessagesByDate({ a: message("2000"), b: message("1000") }, "asc");
    expect(sorted.map(([id]) => id)).toEqual(["b", "a"]);
  });
});

describe("filterMessages", () => {
  const sorted: [string, gmail_v1.Schema$Message][] = [
    ["m3", message("3000")],
    ["m2", message("2000")],
    ["m1", message("1000")],
  ];

  it("keeps messages newer than lastMsgId", () => {
    const result = filterMessages(sorted, "m2");
    expect(Object.keys(result.filteredMessages)).toEqual(["m3"]);
    expect(result.mostRecentMsgId).toBe("m3");
  });

  it("keeps all messages without lastMsgId", () => {
    const result = filterMessages(sorted, null);
    expect(Object.keys(result.filteredMessages)).toEqual(["m3", "m2", "m1"]);
  });

  it("returns nothing when the newest message was already processed", () => {
    const result = filterMessages(sorted, "m3");
    expect(result.filteredMessages).toEqual({});
    expect(result.mostRecentMsgId).toBeNull();
  });
});

describe("extractTextBody", () => {
  it("finds text/plain inside nested multipart parts", () => {
    const payload: gmail_v1.Schema$MessagePart = {
      mimeType: "multipart/mixed",
      parts: [
        {
          mimeType: "multipart/alternative",
          parts: [
            { mimeType: "text/html", body: { data: base64url("<p>html</p>") } },
            { mimeType: "text/plain", body: { data: base64url("ご利用金額 1,000円") } },
          ],
        },
      ],
    };
    expect(extractTextBody(payload)).toBe("ご利用金額 1,000円");
  });

  it("returns null without a text/plain part", () => {
    expect(extractTextBody({ mimeType: "text/html", body: { data: base64url("<p>x</p>") } })).toBeNull();
    expect(extractTextBody(undefined)).toBeNull();
  });
});

describe("stripHtmlTags", () => {
  it("turns block elements into lines and decodes basic entities", () => {
    expect(stripHtmlTags("<style>p{}</style><div>A&amp;B</div><p>C<br>D</p>")).toBe("A&B\n\nC\nD");
  });
});
