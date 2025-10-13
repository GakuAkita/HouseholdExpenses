import { MailParserBase } from "./MailParserBase";

export class AmazonCancelSubscribeMailParser extends MailParserBase {
  constructor(rawText: string, internalDate: string) {
    super(rawText, internalDate);
  }

  extractProductName(): string | null {
    return "";
  }
}
