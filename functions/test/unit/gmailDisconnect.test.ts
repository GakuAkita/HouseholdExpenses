import { describe, expect, it, vi } from "vitest";
import { deleteUserData, DeleteUserDataDeps } from "../../src/jobs/deleteUserData";
import { disconnectGmail, GmailDisconnectDeps, handleDisconnectGmailCall } from "../../src/jobs/gmailDisconnect";
import { encryptWithKey } from "../../src/shared/encryption";

const encryptionKey = Buffer.alloc(32, 7).toString("base64");
const secrets = { clientId: "id", clientSecret: "secret", redirectUri: "https://example.com", encryptionKey };

const setUp = (encryptedTokens: string[] = [encryptWithKey("raw-token", encryptionKey)]) => {
  const steps: string[] = [];
  const step = (name: string) =>
    vi.fn(async () => {
      steps.push(name);
    });
  const revokeToken = vi.fn(async (_token: string): Promise<"revoked" | "already_invalid"> => {
    steps.push("revoke");
    return "revoked";
  });
  const load = vi.fn(async () => secrets);
  const removeGmailTokens = step("removeGmailTokens");
  const firestoreDelete = step("firestoreDelete");
  const rtdbDelete = step("rtdbDelete");
  const deps = {
    runtime: { secrets: { load }, googleOAuthApi: { revokeToken } },
    mailboxExtractionService: {
      getEncryptedGmailRefreshTokens: vi.fn(async () => encryptedTokens),
      removeGmailTokens,
    },
    userService: { deleteUser: firestoreDelete },
    userRTDbService: { deleteUser: rtdbDelete },
  } as unknown as GmailDisconnectDeps & DeleteUserDataDeps;
  return { deps, steps, revokeToken, load, removeGmailTokens, firestoreDelete, rtdbDelete };
};

describe("disconnectGmail", () => {
  it("revokes the decrypted token first, then removes it", async () => {
    const { deps, steps, revokeToken } = setUp();

    await disconnectGmail(deps, "user1");

    expect(revokeToken).toHaveBeenCalledWith("raw-token");
    expect(steps).toEqual(["revoke", "removeGmailTokens"]);
  });

  it("keeps the token when Google can't be reached, so the user can try again", async () => {
    const { deps, revokeToken, removeGmailTokens } = setUp();
    revokeToken.mockRejectedValue(new Error("no response"));

    await expect(disconnectGmail(deps, "user1")).rejects.toThrow("no response");

    expect(removeGmailTokens).not.toHaveBeenCalled();
  });

  it("removes a token Google already considers invalid", async () => {
    const { deps, revokeToken, removeGmailTokens } = setUp();
    revokeToken.mockResolvedValue("already_invalid");

    await disconnectGmail(deps, "user1");

    expect(removeGmailTokens).toHaveBeenCalledWith("user1");
  });

  it("removes a token that can't be decrypted, because it can never be revoked", async () => {
    const { deps, revokeToken, removeGmailTokens } = setUp(["v2:not:a-valid-token"]);

    await disconnectGmail(deps, "user1");

    expect(revokeToken).not.toHaveBeenCalled();
    expect(removeGmailTokens).toHaveBeenCalledWith("user1");
  });

  it("doesn't load the secrets or call Google when Gmail isn't connected", async () => {
    const { deps, load, revokeToken } = setUp([]);

    await disconnectGmail(deps, "user1");

    expect(load).not.toHaveBeenCalled();
    expect(revokeToken).not.toHaveBeenCalled();
  });
});

describe("handleDisconnectGmailCall", () => {
  it("rejects a caller who isn't signed in, without touching anything", async () => {
    const { deps, revokeToken, removeGmailTokens } = setUp();

    await expect(handleDisconnectGmailCall(deps, undefined)).rejects.toMatchObject({ code: "unauthenticated" });

    expect(revokeToken).not.toHaveBeenCalled();
    expect(removeGmailTokens).not.toHaveBeenCalled();
  });

  it("disconnects the signed-in user", async () => {
    const { deps, removeGmailTokens } = setUp();

    await handleDisconnectGmailCall(deps, "user1");

    expect(removeGmailTokens).toHaveBeenCalledWith("user1");
  });

  it("gives the app a message without the failure's details", async () => {
    const { deps, revokeToken } = setUp();
    revokeToken.mockRejectedValue(new Error("internal detail"));

    await expect(handleDisconnectGmailCall(deps, "user1")).rejects.toMatchObject({
      code: "unavailable",
      message: "Couldn't disconnect Gmail. Please try again.",
    });
  });
});

describe("deleteUserData", () => {
  it("revokes Gmail access, then deletes Firestore, then the Realtime Database", async () => {
    const { deps, steps } = setUp();

    await deleteUserData(deps, "user1");

    expect(steps).toEqual(["revoke", "firestoreDelete", "rtdbDelete"]);
  });

  it("deletes nothing when revoking fails, so the retry can still revoke the token", async () => {
    const { deps, revokeToken, firestoreDelete, rtdbDelete } = setUp();
    revokeToken.mockRejectedValue(new Error("no response"));

    await expect(deleteUserData(deps, "user1")).rejects.toThrow("no response");

    expect(firestoreDelete).not.toHaveBeenCalled();
    expect(rtdbDelete).not.toHaveBeenCalled();
  });

  it("keeps the Realtime Database when Firestore fails, so the retry finishes the job", async () => {
    const { deps, firestoreDelete, rtdbDelete } = setUp();
    firestoreDelete.mockRejectedValue(new Error("firestore down"));

    await expect(deleteUserData(deps, "user1")).rejects.toThrow("firestore down");

    expect(rtdbDelete).not.toHaveBeenCalled();
  });

  it("still deletes the data when the user never connected Gmail", async () => {
    const { deps, firestoreDelete, rtdbDelete } = setUp([]);

    await deleteUserData(deps, "user1");

    expect(firestoreDelete).toHaveBeenCalledWith("user1");
    expect(rtdbDelete).toHaveBeenCalledWith("user1");
  });
});
