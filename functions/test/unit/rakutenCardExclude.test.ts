import { describe, expect, it } from "vitest";
import { AssignmentCondition } from "../../src/constants/AssignmentCondition";
import { isExcludedStore } from "../../src/domain/rakutenCardExclude";

describe("isExcludedStore", () => {
  it("matches the whole store name for an exact match", () => {
    const items = { a: { name: "ローソン", condition: AssignmentCondition.EXACT_MATCH } };
    expect(isExcludedStore("ローソン", items)).toBe(true);
    expect(isExcludedStore("ローソン 駅前店", items)).toBe(false);
  });

  it("matches a part of the store name for contains", () => {
    const items = { a: { name: "ローソン", condition: AssignmentCondition.CONTAINS } };
    expect(isExcludedStore("ローソン 駅前店", items)).toBe(true);
    expect(isExcludedStore("ドラッグストア", items)).toBe(false);
  });

  it("excludes a store when any of the items matches", () => {
    const items = {
      a: { name: "ドラッグストア", condition: AssignmentCondition.EXACT_MATCH },
      b: { name: "ローソン", condition: AssignmentCondition.CONTAINS },
    };
    expect(isExcludedStore("ローソン 駅前店", items)).toBe(true);
  });

  it("excludes nothing without items", () => {
    expect(isExcludedStore("ローソン", {})).toBe(false);
  });

  it("ignores an item without a name, instead of excluding every store", () => {
    expect(isExcludedStore("ローソン", { a: { name: "", condition: AssignmentCondition.CONTAINS } })).toBe(false);
    expect(isExcludedStore("ローソン", { a: { condition: AssignmentCondition.CONTAINS } })).toBe(false);
  });

  it("ignores an item with an unknown condition", () => {
    expect(isExcludedStore("ローソン", { a: { name: "ローソン", condition: "UNKNOWN" } })).toBe(false);
    expect(isExcludedStore("ローソン", { a: { name: "ローソン" } })).toBe(false);
  });
});
