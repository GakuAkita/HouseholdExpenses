import { Firestore } from "firebase-admin/firestore";
import { Category } from "../../type/Category";
import {
  FuncResult,
  FuncResultWithData,
  FuncStatus,
} from "../../type/FuncStatus";

export class CategoryService {
  private db: Firestore;

  constructor(db: Firestore) {
    this.db = db;
  }

  private getUserCategoriesColRef(userId: string) {
    return this.db.collection("users").doc(userId).collection("categories");
  }

  async getAllCategories(
    userId: string
  ): Promise<FuncResultWithData<Record<string, Category>>> {
    try {
      const ref = this.getUserCategoriesColRef(userId);
      const snapshot = await ref.get();

      const categoryMap: Record<string, Category> = {};
      if (snapshot.empty) {
        return {
          status: FuncStatus.SUCCESS,
          message: "No Category found.",
          data: {},
        };
      }

      snapshot.forEach((doc) => {
        const data = doc.data() as Category;
        categoryMap[doc.id] = data;
      });
      return {
        status: FuncStatus.SUCCESS,
        message: "getAllCategory Success",
        data: categoryMap,
      };
    } catch (e: any) {
      return {
        status: FuncStatus.ERROR,
        message: e.message || "Failed to get Categories",
      };
    }
  }

  async addCategory(
    userId: string,
    expenseData: Category
  ): Promise<FuncResult> {
    try {
      const ref = this.getUserCategoriesColRef(userId);
      await ref.add(expenseData);
      return {
        status: FuncStatus.SUCCESS,
        message: `Category added`,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to add category: ${error.message}`,
      };
    }
  }
}
