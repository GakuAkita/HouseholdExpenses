import { describe, expect, it, vi } from "vitest";
import { GmailApiClient } from "../../src/infra/gmail/GmailApiClient";
import {
  getAmazonDispatchedMailIds,
  getAmazonItemMailIds,
  getAmazonKindleMailIds,
  getAmazonSubscribeNextShipNotifyAndCancelMailIds,
  getRakutenCardETCMailIds,
  getRakutenPayMailIds,
  getShikokuElectricMailIds,
  getUdemyMailIds,
} from "../../src/infra/gmail/mailQueries";
import { FuncStatus } from "../../src/type/FuncStatus";

/* Records the Gmail query each function sends. before: is endTime + 1. */
const queryOf = async (
  run: (client: GmailApiClient) => Promise<unknown>
): Promise<{ query: string; maxResults: number | undefined }> => {
  const queryMessages = vi.fn().mockResolvedValue({ status: FuncStatus.SUCCESS, data: [] });
  await run({ queryMessages } as unknown as GmailApiClient);
  const [query, maxResults] = queryMessages.mock.calls[0];
  return { query, maxResults };
};

describe("mail queries", () => {
  it("builds the query for each mail type", async () => {
    expect((await queryOf((c) => getRakutenPayMailIds(c, 100, 200))).query).toBe(
      "subject:{楽天ペイアプリご利用内容確認メール 楽天ペイお支払い完了のお知らせ} from:no-reply@pay.rakuten.co.jp after:100 before:201"
    );
    expect((await queryOf((c) => getAmazonKindleMailIds(c, 100, 200))).query).toBe(
      "from:digital-no-reply@amazon.co.jp Kindle -subject:予約注文 after:100 before:201"
    );
    expect((await queryOf((c) => getShikokuElectricMailIds(c, 100, 200))).query).toBe(
      "from:yonden-con@yonden.co.jp 【四国電力】電気料金等のお知らせ after:100 before:201"
    );
    expect((await queryOf((c) => getAmazonItemMailIds(c, 100, 200))).query).toBe(
      "from:auto-confirm@amazon.co.jp after:100 before:201"
    );
    expect((await queryOf((c) => getUdemyMailIds(c, 100, 200))).query).toBe(
      "from:hello@alerts.udemy.com after:100 before:201"
    );
    expect((await queryOf((c) => getRakutenCardETCMailIds(c, 100, 200))).query).toBe(
      "from:info@mail.rakuten-card.co.jp ETCカード売上 after:100 before:201"
    );
  });

  it("passes the maximum number of results for Amazon", async () => {
    expect(await queryOf((c) => getAmazonDispatchedMailIds(c, 100, 200, 10))).toEqual({
      query: "from:shipment-tracking@amazon.co.jp subject:発送済み after:100 before:201",
      maxResults: 10,
    });
    const subscribe = await queryOf((c) => getAmazonSubscribeNextShipNotifyAndCancelMailIds(c, 100, 200));
    expect(subscribe.query).toMatch(/^from:no-reply@amazon\.co\.jp subject:".+"( OR ".+"){3} after:100 before:201$/);
    expect(subscribe.maxResults).toBe(10);
  });
});
