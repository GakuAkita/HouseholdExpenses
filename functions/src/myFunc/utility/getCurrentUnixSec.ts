/* 整数で返ってくる */
export const convertUnixMillisecToSec = (milliSec: number): number => {
  return Math.floor(milliSec / 1000);
};

/**
 * UnixタイムをDateStringに変換
 */
export const convertUnixMillisecToDateString = (milliSec: number): string => {
  const date = new Date(milliSec);
  return date.toISOString(); // 例: "2025-07-27T12:34:56.789Z"
};
