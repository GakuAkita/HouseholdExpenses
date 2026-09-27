export function sanitizeEmail(email: string): string {
  return email.replace(/\./g, "__dot__").replace(/@/g, "__at__");
}
