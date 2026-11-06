import { logger } from "firebase-functions";
import { AssignmentCondition } from "../constants/AssignmentCondition";
import { RakutenCardExcludeItem } from "../type/Mailbox";

/** True when the user chose not to save charges at this store. */
export const isExcludedStore = (
  storeName: string,
  items: Record<string, RakutenCardExcludeItem>
): boolean =>
  Object.values(items).some((item) => {
    /* Every store name contains an empty name, so this item would exclude every charge. */
    if (!item.name) {
      logger.error(`Invalid exclude item found: ${JSON.stringify(item)}`);
      return false;
    }
    switch (item.condition) {
      case AssignmentCondition.EXACT_MATCH:
        return storeName === item.name;
      case AssignmentCondition.CONTAINS:
        return storeName.includes(item.name);
      default:
        logger.error(`Invalid exclude item found: ${JSON.stringify(item)}`);
        return false;
    }
  });
