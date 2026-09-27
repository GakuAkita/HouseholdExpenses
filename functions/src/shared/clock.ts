/**
 * The current time. Code that needs "now" takes a Clock, so tests can fix the time.
 */
export interface Clock {
  now(): Date;
}

export const systemClock: Clock = {
  now: () => new Date(),
};

/** A clock that always returns the given time. */
export const fixedClock = (date: Date): Clock => ({
  now: () => new Date(date.getTime()),
});
