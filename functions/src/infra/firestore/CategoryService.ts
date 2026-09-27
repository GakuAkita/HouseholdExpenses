import { Firestore } from "firebase-admin/firestore";
import { Category } from "../../type/Category";

export class CategoryService {
  constructor(private db: Firestore) {}

  private getUserCategoriesColRef(userId: string) {
    return this.db.collection("users").doc(userId).collection("categories");
  }

  /** The user's categories by document id. Empty when the user has none. */
  async getAllCategories(userId: string): Promise<Record<string, Category>> {
    const snapshot = await this.getUserCategoriesColRef(userId).get();
    const categories: Record<string, Category> = {};
    snapshot.forEach((doc) => {
      categories[doc.id] = doc.data() as Category;
    });
    return categories;
  }
}
