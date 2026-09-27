import * as dotenv from "dotenv";
import * as path from "path";
import { defineConfig } from "vitest/config";
import { sharedTestOptions } from "./vitest.shared";

/*
 * Live tests: call real external services (Gmail) with the credentials in ../.env.local.
 * They are skipped when the credentials are not set. Run them by hand: npm run test:live
 */
dotenv.config({ path: path.resolve(__dirname, "../.env.local") });

export default defineConfig({
  test: {
    ...sharedTestOptions,
    include: ["test/live/**/*.test.ts"],
    testTimeout: 30_000,
  },
});
