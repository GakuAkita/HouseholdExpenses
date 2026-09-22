export enum TimeZone {
  JST = "Asia/Tokyo", // 日本標準時
  UTC = "UTC", // 協定世界時
  TAIPEI = "Asia/Taipei",
  AMERICAN_EAST = "America/New_York",
  AMERICAN_WEST = "America/Los_Angeles",
  PARIS = "Europe/Paris",
}

export const TriggerTimeZone = TimeZone.JST; // トリガーのタイムゾーン。デフォルトは日本時間
