/* 新規登録時のメールも検知してしまうと二重登録してしまう。 */
export const AmazonMailSubjects = {
  NEXT_SHIPMENT: "次回の配送を確認する",
  PRICE_CHANGED: "価格の変更: 次回の配達をご確認ください",
  ITEM_RUNOUT: "在庫切れ商品:次回の配送を確認する",
  CANCELED_SUBSCRIPTION: "定期購入はキャンセルされました",
} as const;

// 型も自動で抽出できる
export type AmazonMailSubject =
  (typeof AmazonMailSubjects)[keyof typeof AmazonMailSubjects];
