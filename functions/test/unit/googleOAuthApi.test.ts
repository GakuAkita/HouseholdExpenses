import axios, { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from "axios";
import { afterEach, describe, expect, it, vi } from "vitest";
import { googleOAuthApi } from "../../src/infra/gmail/GoogleOAuthApi";

const secretToken = "1//secret-refresh-token";

/** A failed request, carrying the request body the way a real AxiosError does. */
const axiosFailure = (status: number | undefined, data: unknown = {}) => {
  const config = { data: `token=${secretToken}` } as InternalAxiosRequestConfig;
  const response = status ? ({ status, data, config } as AxiosResponse) : undefined;
  return new AxiosError("Request failed", status ? undefined : "ECONNRESET", config, undefined, response);
};

describe("googleOAuthApi.revokeToken", () => {
  afterEach(() => vi.restoreAllMocks());

  it("sends the token in the form body and returns revoked", async () => {
    const post = vi.spyOn(axios, "post").mockResolvedValue({ status: 200 });

    expect(await googleOAuthApi.revokeToken(secretToken)).toBe("revoked");

    const [url, body] = post.mock.calls[0];
    expect(url).toBe("https://oauth2.googleapis.com/revoke");
    expect(url).not.toContain("secret");
    expect(body).toBe(`token=${encodeURIComponent(secretToken)}`);
  });

  it("returns already_invalid when Google no longer accepts the token", async () => {
    vi.spyOn(axios, "post").mockRejectedValue(axiosFailure(400, { error: "invalid_token" }));

    expect(await googleOAuthApi.revokeToken(secretToken)).toBe("already_invalid");
  });

  it.each([
    ["another 400 error", axiosFailure(400, { error: "invalid_request" })],
    ["a server error", axiosFailure(503)],
    ["no response", axiosFailure(undefined)],
    ["an unexpected error", new Error(`boom ${secretToken}`)],
  ])("throws an error without the token for %s", async (_, failure) => {
    vi.spyOn(axios, "post").mockRejectedValue(failure);

    const error = await googleOAuthApi.revokeToken(secretToken).catch((e) => e);

    expect(error).toBeInstanceOf(Error);
    expect(axios.isAxiosError(error)).toBe(false);
    expect(JSON.stringify(error, Object.getOwnPropertyNames(error))).not.toContain(secretToken);
  });
});
