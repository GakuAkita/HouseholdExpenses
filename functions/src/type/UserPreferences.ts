export const UserPreferencesKeys = {
  TIME_ZONE: "timeZone",
} as const;

export interface UserPreferences {
  [UserPreferencesKeys.TIME_ZONE]: string;
}
