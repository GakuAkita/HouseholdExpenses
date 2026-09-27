import { randomBytes } from "crypto";
import { describe, expect, it } from "vitest";
import { convertDaysToNums, convertDayToNum, DayOfWeekNum } from "../../src/constants/DayOfWeek";
import { convertyyyymmddToUTCIsoString } from "../../src/shared/dateConverter";
import { decryptWithKey, encryptWithKey } from "../../src/shared/encryption";
import {
  convertUnixMillisecToDateString,
  convertUnixMillisecToSec,
} from "../../src/shared/unixTime";
import { isAmazonSubscribeProductExist } from "../../src/domain/isAmazonSubscribeProductExist";
import { FuncStatus } from "../../src/type/FuncStatus";

describe("convertDayToNum", () => {
  it("converts Kotlin DayOfWeek values (1 = Monday ... 7 = Sunday)", () => {
    expect(convertDayToNum(1)).toBe(DayOfWeekNum.MON);
    expect(convertDayToNum(7)).toBe(DayOfWeekNum.SUN);
    expect(convertDayToNum(0)).toBe(DayOfWeekNum.SUN);
    expect(convertDayToNum(8)).toBeNull();
  });

  it("converts names stored by the app", () => {
    expect(convertDayToNum("MONDAY")).toBe(DayOfWeekNum.MON);
    expect(convertDayToNum("fri")).toBe(DayOfWeekNum.FRI);
    expect(convertDayToNum("7")).toBe(DayOfWeekNum.SUN);
    expect(convertDayToNum("someday")).toBeNull();
  });

  it("convertDaysToNums drops values it can't convert", () => {
    expect(convertDaysToNums(["MONDAY", "FRIDAY", "x"])).toEqual([DayOfWeekNum.MON, DayOfWeekNum.FRI]);
  });
});

describe("encryption", () => {
  const key = randomBytes(32).toString("base64");

  it("decrypts what it encrypted", () => {
    const encrypted = encryptWithKey("refresh-token", key);
    expect(encrypted).toMatch(/^[0-9a-f]{32}:[0-9a-f]+$/);
    expect(decryptWithKey(encrypted, key)).toBe("refresh-token");
  });

  it("uses a new IV every time", () => {
    expect(encryptWithKey("same", key)).not.toBe(encryptWithKey("same", key));
  });

  it("rejects data without the iv:data format", () => {
    expect(() => decryptWithKey("no-separator", key)).toThrow("Invalid encrypted data format");
  });
});

describe("date helpers", () => {
  it("convertUnixMillisecToSec floors to seconds", () => {
    expect(convertUnixMillisecToSec(1_700_000_000_999)).toBe(1_700_000_000);
  });

  it("convertUnixMillisecToDateString returns an ISO string", () => {
    expect(convertUnixMillisecToDateString(0)).toBe("1970-01-01T00:00:00.000Z");
  });

  it("convertyyyymmddToUTCIsoString uses midnight in the runtime zone (UTC on Cloud Functions)", () => {
    /* Current behaviour: not midnight JST. Kept as-is by the refactor. */
    expect(convertyyyymmddToUTCIsoString("2026/09/15")).toBe("2026-09-15T00:00:00.000Z");
  });
});

describe("isAmazonSubscribeProductExist", () => {
  const items = {
    item1: { id: "item1", productName: "サントリー 天然水 2L×9本" },
  };

  it("matches when one name starts with the other", () => {
    expect(isAmazonSubscribeProductExist({ productName: "サントリー 天然水" }, items)).toEqual({
      status: FuncStatus.SUCCESS,
      data: "item1",
    });
    expect(
      isAmazonSubscribeProductExist({ productName: "サントリー 天然水 2L×9本 [Amazon.co.jp限定]" }, items)
        .status
    ).toBe(FuncStatus.SUCCESS);
  });

  it("returns EMPTY when no name matches", () => {
    expect(isAmazonSubscribeProductExist({ productName: "コカ・コーラ" }, items).status).toBe(
      FuncStatus.EMPTY
    );
  });

  it("returns ERROR for an empty name, or when a registered item has no name", () => {
    expect(isAmazonSubscribeProductExist({ productName: " " }, items).status).toBe(FuncStatus.ERROR);
    expect(
      isAmazonSubscribeProductExist({ productName: "A" }, { broken: { id: "broken" } }).status
    ).toBe(FuncStatus.ERROR);
  });
});
