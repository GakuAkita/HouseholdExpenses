import { logger } from "firebase-functions";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { AmazonSubscribeItem } from "../../type/Mailbox";

export function isAmazonSubscribeProductExist(
  item: AmazonSubscribeItem,
  itemMap: Record<string, AmazonSubscribeItem>
): FuncResultWithData<string> {
  /* これってもしかしたら参照渡しかもしれないので、この中で書き換えないように注意 */
  const targetName = item.productName?.trim();
  if (!targetName) {
    return {
      status: FuncStatus.ERROR,
      message: `item's product name is empty`,
    };
  }
  for (const [id, itemInMap] of Object.entries(itemMap)) {
    const productName = itemInMap.productName?.trim();
    if (!productName) {
      return {
        status: FuncStatus.ERROR,
        message: `${item.id} doesn't have productName`,
      };
    }

    /**
     * targetNameが引数に渡されるほう
     * productNameがmapに登録されているほう
     */
    logger.debug(`isAmazonSubscribeProductExist:  targetName: ${targetName}, productName: ${productName}`);

    const shorter =
      targetName.length <= productName.length ? targetName : productName;

    /**
     *  targetName.length >= productName.lengthになってしまうと、強制的に存在する扱いになっていしまう！！
     *  なぜなら両方にproductNameが入ってしまうから。
     *  そのため、targetName.length > productName.lengthになっている方をlongerとしている。
     *  確率的にはあまりないんだが、targetNamneとproductNameが同じ文字数になってしまうケースが有る、、
     */
    const longer =
      targetName.length > productName.length ? targetName : productName;

    if (longer.startsWith(shorter)) {
      logger.log(`isAmazonSubscribeProductExist: Found ${item.productName} in the map.`);
      logger.debug(`isAmazonSubscribeProductExist: !!!${longer} starts with ${shorter}!!!`);
      return {
        status: FuncStatus.SUCCESS,
        data: id,
      };
    }
  }
  logger.log(`isAmazonSubscribeProductExist: Not found ${item.productName} in the map.`);
  /* ここまで来たら新規追加 */
  return {
    status: FuncStatus.EMPTY,
  };
}
