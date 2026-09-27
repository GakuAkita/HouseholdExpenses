import { defineConfig } from "vitest/config";

/*
 * Cloud Functions run in UTC. Some date helpers use the runtime's local time zone,
 * so tests pin TZ to UTC to behave the same as production on any machine.
 */
process.env.TZ = "UTC";

export default defineConfig({
  test: {
    include: ["test/**/*.test.ts"],
    env: { TZ: "UTC" },
    /* The code logs a lot through firebase-functions. Show it only for failing tests. */
    silent: "passed-only",
  },
});
