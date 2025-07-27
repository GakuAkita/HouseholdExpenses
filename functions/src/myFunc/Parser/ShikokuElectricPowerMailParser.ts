import { logger } from "firebase-functions";

export class ShikokuElectricPowerMailParser {
  constructor(private rawText: string, private internalDate: string) {}

  extractDate(): string | null {
    logger.info(`internalDate:${this.internalDate}`);
    return "";
  }
}
