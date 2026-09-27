import * as admin from "firebase-admin";
import { describe, expect, it } from "vitest";
import * as index from "../../src/index";

describe("index", () => {
  it("doesn't initialize Firebase when it is imported", () => {
    expect(admin.apps).toHaveLength(0);
  });

  it("exports the same function names as the deployed functions", () => {
    /* Renaming an export makes a deploy create a new function and delete the old one. */
    /* "default" is added by the CommonJS interop of the test runner. */
    expect(Object.keys(index).filter((key) => key !== "default").sort()).toEqual([
      "handleOAuthCallback",
      "mailboxExtractionJob_daily",
      "mailboxExtractionJob_shortPeriod",
      "monthly_repeatAddJob",
      "onUserCreate",
    ]);
  });
});
