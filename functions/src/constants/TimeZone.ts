export enum TimeZone {
  JST = "Asia/Tokyo", // 日本標準時
  UTC = "UTC", // 協定世界時
}

export const TriggerTimeZone = TimeZone.JST; // トリガーのタイムゾーン。デフォルトは日本時間
