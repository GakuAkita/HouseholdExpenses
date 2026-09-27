/** The message of a caught value, for logs. */
export const messageOf = (error: unknown): string =>
  error instanceof Error ? error.message : String(error);
