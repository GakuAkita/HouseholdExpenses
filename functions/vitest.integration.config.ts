import { defineConfig } from "vitest/config";
import { sharedTestOptions } from "./vitest.shared";

/*
 * Integration tests: run against the Firebase emulators (`firebase emulators:start`).
 * npm run test:integration. The hosts can be overridden with the usual environment variables.
 */
export default defineConfig({
  test: {
    ...sharedTestOptions,
    include: ["test/integration/**/*.test.ts"],
    env: {
      ...sharedTestOptions.env,
      GCLOUD_PROJECT: process.env.GCLOUD_PROJECT ?? "householdexpenses2",
      FIRESTORE_EMULATOR_HOST: process.env.FIRESTORE_EMULATOR_HOST ?? "127.0.0.1:5002",
      FIREBASE_DATABASE_EMULATOR_HOST: process.env.FIREBASE_DATABASE_EMULATOR_HOST ?? "127.0.0.1:9000",
      FIREBASE_AUTH_EMULATOR_HOST: process.env.FIREBASE_AUTH_EMULATOR_HOST ?? "127.0.0.1:9099",
    },
    testTimeout: 20_000,
    hookTimeout: 20_000,
  },
});
