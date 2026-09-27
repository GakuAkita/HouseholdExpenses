import { Database } from "firebase-admin/database";
import { CategoryAssignmentData } from "../../type/CategoryAssignment";

export class CategoryAssignmentService {
  constructor(private db: Database) {}

  /** The user's category assignment rules. Empty lists when the user has none. */
  async getCategoryAssignmentData(userId: string): Promise<CategoryAssignmentData> {
    const snapshot = await this.db.ref("users").child(userId).child("category_assignment_data").get();
    const data: Partial<CategoryAssignmentData> | null = snapshot.val();
    return {
      storeName: data?.storeName ?? {},
      productName: data?.productName ?? {},
    };
  }
}
