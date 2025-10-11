import { MailboxExtractionService } from "../RealtimeDbService/MailboxExtractionService";

/**
 * Gmailをモニターし、
 * Amazon定期便のリストを更新
 */
class AmazonSubscribeListProcessor {
  private userId: string;

  constructor(
    userId: string,
    private mailboxExtractionService: MailboxExtractionService
  ) {
    this.userId = userId;
  }
}
