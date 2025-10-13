export const AmazonMailSubjects = {
  NEXT_SHIPMENT: "次回の配送を確認する",
  CANCELED_SUBSCRIPTION: "定期購入はキャンセルされました",
} as const;

// 型も自動で抽出できる
export type AmazonMailSubject =
  (typeof AmazonMailSubjects)[keyof typeof AmazonMailSubjects];
