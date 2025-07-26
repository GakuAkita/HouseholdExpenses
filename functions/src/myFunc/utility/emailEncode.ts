export function sanitizeEmail(email: string): string {
  return email.replace(/\./g, "__dot__").replace(/@/g, "__at__");
}

export function restoreEmail(safeEmail: string): string {
  return safeEmail.replace(/__at__/g, "@").replace(/__dot__/g, ".");
}
