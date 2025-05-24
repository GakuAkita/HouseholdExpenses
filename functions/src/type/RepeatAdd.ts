import { Expense } from "./Expense";

export interface Frequency {
  frequency?: string; // "everyday" | "weekly" | "monthly" | "yearly" としてもOK
  month?: number;
  day?: number;
  dayOfWeek?: string[];
  hour?: number;
  minute?: number;
}

export interface RepeatAdd {
  id?: string;
  timestamp?: number;
  expense: Expense;
  frequencyInfo: Frequency;
}
