import { logger } from "firebase-functions";
import { AmazonSubscribeItem } from "../type/Mailbox";

/**
 * The id of the registered subscribe item for the product, or null when none matches.
 * Names match when one starts with the other, because mails shorten long product names.
 * Throws when the product name or a registered item's name is empty.
 */
export function findAmazonSubscribeItemId(
  productName: string | undefined,
  itemMap: Record<string, AmazonSubscribeItem>
): string | null {
  const targetName = productName?.trim();
  if (!targetName) {
    throw new Error(`item's product name is empty`);
  }
  for (const [id, itemInMap] of Object.entries(itemMap)) {
    const registeredName = itemInMap.productName?.trim();
    if (!registeredName) {
      throw new Error(`${id} doesn't have productName`);
    }
    logger.debug(`findAmazonSubscribeItemId: targetName: ${targetName}, productName: ${registeredName}`);

    /**
     * targetName.length >= productName.lengthになってしまうと、強制的に存在する扱いになっていしまう！！
     * そのため、長い方をlonger、短い方をshorterとして、longerがshorterで始まるかを見る。
     */
    const shorter = targetName.length <= registeredName.length ? targetName : registeredName;
    const longer = targetName.length > registeredName.length ? targetName : registeredName;
    if (longer.startsWith(shorter)) {
      logger.log(`findAmazonSubscribeItemId: Found ${productName} in the map.`);
      return id;
    }
  }
  logger.log(`findAmazonSubscribeItemId: Not found ${productName} in the map.`);
  return null;
}
