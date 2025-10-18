import { TimeZone } from "../constants/TimeZone";

export const UserPreferencesKeys = {
  TIME_ZONE: "timeZone",
} as const;

export interface UserPreferences {
  [UserPreferencesKeys.TIME_ZONE]: string;
}

/* ユーザーが追加されたときは */
export const defaultUserPreferences = {
  [UserPreferencesKeys.TIME_ZONE]: TimeZone.JST,
};
