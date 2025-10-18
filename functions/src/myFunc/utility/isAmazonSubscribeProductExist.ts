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
  for (const [id, item] of Object.entries(itemMap)) {
    const productName = item.productName?.trim();
    if (!productName) {
      return {
        status: FuncStatus.ERROR,
        message: `${item.id} doesn't have productName`,
      };
    }

    const shorter =
      targetName.length <= productName.length ? targetName : productName;
    const longer =
      targetName.length >= productName.length ? targetName : productName;
    if (longer.startsWith(shorter)) {
      return {
        status: FuncStatus.SUCCESS,
        data: id,
      };
    }
  }

  /* ここまで来たら新規追加 */
  return {
    status: FuncStatus.EMPTY,
  };
}
