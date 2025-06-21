export interface MailboxTokenType {
  refreshToken: string /* 暗号化されている */;
  timestamp?: string;/* ISO 8601形式のタイムスタンプ */;
}
