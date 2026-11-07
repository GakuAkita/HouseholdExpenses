import axios from "axios";
import { afterEach, describe, expect, it, vi } from "vitest";
import { GmailApiClient } from "../../src/infra/gmail/GmailApiClient";

const config = {
  clientId: "test-client-id",
  clientSecret: "test-client-secret",
  refreshToken: "1//test-refresh-token",
};

describe("GmailApiClient.authorize", () => {
  afterEach(() => vi.restoreAllMocks());

  it("sends the refresh token and client secret in the form body, not in the URL", async () => {
    const post = vi.spyOn(axios, "post").mockResolvedValue({ data: { access_token: "access" } });

    expect(await new GmailApiClient(config).authorize()).toBe("access");

    const [url, body, options] = post.mock.calls[0];
    expect(url).toBe("https://oauth2.googleapis.com/token");
    expect(options?.params).toBeUndefined();
    expect(options?.headers).toMatchObject({ "Content-Type": "application/x-www-form-urlencoded" });
    expect(Object.fromEntries(new URLSearchParams(body as string))).toEqual({
      client_id: config.clientId,
      client_secret: config.clientSecret,
      refresh_token: config.refreshToken,
      grant_type: "refresh_token",
    });
  });

  it("requests an access token once per client", async () => {
    const post = vi.spyOn(axios, "post").mockResolvedValue({ data: { access_token: "access" } });
    const client = new GmailApiClient(config);

    await client.authorize();
    await client.authorize();

    expect(post).toHaveBeenCalledTimes(1);
  });

  it("throws when the response has no access token", async () => {
    vi.spyOn(axios, "post").mockResolvedValue({ data: {} });

    await expect(new GmailApiClient(config).authorize()).rejects.toThrow("Access token not found");
  });
});
