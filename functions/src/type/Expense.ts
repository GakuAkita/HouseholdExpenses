import { Category } from "./Category"; // adjust the path as needed

export interface Expense {
  id?: string;
  generatedType?: string;
  datetime?: string;
  timestamp?: number;
  amount?: number;
  category?: Category | null;
  note?: string;
  storeName?: string;
  itemName?: string;
}
