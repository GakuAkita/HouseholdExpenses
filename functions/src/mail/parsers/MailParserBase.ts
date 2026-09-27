import { convertUnixMillisecToDateString } from "../../shared/unixTime";

/** Thrown when a mail doesn't have the expected content. The mail is skipped. */
export class MailParseError extends Error {
  name = "MailParseError";
}

export class MailParserBase {
  constructor(protected rawText: string, protected internalDate?: string) {}

  extractDate(): string | null {
    const milliSec = Number(this.internalDate);
    const dateStr = convertUnixMillisecToDateString(milliSec);
    return dateStr;
  }
}
