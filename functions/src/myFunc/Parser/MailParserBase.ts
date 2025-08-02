import { convertUnixMillisecToDateString } from "../utility/getCurrentUnixSec";

export class MailParserBase {
  constructor(protected rawText: string, protected internalDate: string) {}

  extractDate(): string | null {
    const milliSec = Number(this.internalDate);
    const dateStr = convertUnixMillisecToDateString(milliSec);
    return dateStr;
  }
}
