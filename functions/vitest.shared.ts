/*
 * Cloud Functions run in UTC. Some date helpers use the runtime's local time zone,
 * so tests pin TZ to UTC to behave the same as production on any machine.
 */
process.env.TZ = "UTC";

export const sharedTestOptions = {
  env: { TZ: "UTC" },
  /* The code logs a lot through firebase-functions. Show it only for failing tests. */
  silent: "passed-only" as const,
};
