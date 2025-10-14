import { AmazonSubscribeItem } from "../../type/Mailbox";
import { MailParserBase } from "./MailParserBase";

export class AmazonCancelSubscribeMailParser extends MailParserBase {
  constructor(rawText: string, internalDate: string) {
    super(rawText, internalDate);
  }

  extractProductName(): string | null {
    return "";
  }

  extractPrice(): number {
    return 0;
  }

  extractQuantify(): number {
    return 1;
  }

  toSubscribeItem(): AmazonSubscribeItem {
    return {
      productName: "aa",
      quantity: 1 /* たぶん使わない */,
    };
  }
}
