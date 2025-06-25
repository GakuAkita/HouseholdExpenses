import { GmailApiClient } from "../Client/GmailApiClient";
import { MailboxExtractionService } from "./../FirestoreService/MailboxExtractionService";
export class MailboxExtractionProcessor {
  constructor(
    mailbxoExtractionService: MailboxExtractionService,
    gmailApiClient: GmailApiClient
  ) {}
}
