import { describe, expect, it, vi } from "vitest";
import { forEachUser } from "../../src/jobs/forEachUser";
import { runRepeatAddJob } from "../../src/jobs/scheduledJobs";
import { Services } from "../../src/app/services";

const userService = (userIds: string[]) => ({
  getAllUserIds: async () => userIds,
});

describe("forEachUser", () => {
  it("runs the task for every user in order", async () => {
    const seen: string[] = [];
    await forEachUser(userService(["a", "b", "c"]), async (userId) => {
      seen.push(userId);
    });
    expect(seen).toEqual(["a", "b", "c"]);
  });

  it("continues with the next user after an exception", async () => {
    const seen: string[] = [];
    await forEachUser(userService(["a", "b", "c"]), async (userId) => {
      if (userId === "b") throw new Error("boom");
      seen.push(userId);
    });
    expect(seen).toEqual(["a", "c"]);
  });

  it("does nothing when the users can't be read", async () => {
    const task = vi.fn();
    await forEachUser({ getAllUserIds: () => Promise.reject(new Error("unavailable")) }, task);
    expect(task).not.toHaveBeenCalled();
  });
});

describe("runRepeatAddJob", () => {
  it("adds RepeatAdd expenses for every user", async () => {
    const addExpensesFromAllRepeatAdd = vi.fn().mockResolvedValue(1);

    await runRepeatAddJob({
      userService: userService(["a", "b"]),
      repeatAddProcessor: { addExpensesFromAllRepeatAdd },
    } as unknown as Pick<Services, "userService" | "repeatAddProcessor">);

    expect(addExpensesFromAllRepeatAdd.mock.calls).toEqual([["a"], ["b"]]);
  });
});
