import { defineConfig } from "vitest/config";
import { sharedTestOptions } from "./vitest.shared";

/* Unit tests: no emulator or network needed. npm test */
export default defineConfig({
  test: {
    ...sharedTestOptions,
    include: ["test/unit/**/*.test.ts"],
  },
});
