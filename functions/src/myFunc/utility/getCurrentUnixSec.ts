export const getCurrentUnixMillisec = (): number => {
  const nowSeconds = Date.now();
  return nowSeconds;
};

/* 整数で返ってくる */
export const convertUnixMillisecToSec = (milliSec: number): number => {
  return Math.floor(milliSec / 1000);
};

export const getCurrentUnixSec = (): number => {
  return convertUnixMillisecToSec(getCurrentUnixMillisec());
};
