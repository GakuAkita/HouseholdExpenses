import { Database, Reference } from "firebase-admin/database";
import { AssignmentCondition } from "../../constants/AssignmentCondition";
import { CategoryAssignmentData } from "../../type/CategoryAssignment";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";

export class CategoryAssignmentService {
  private db: Database;

  constructor(db: Database) {
    this.db = db;
  }

  private getUserCategoryAssignmentDataRef(userId: string): Reference {
    return this.db.ref("users").child(userId).child("category_assignment_data");
  }

  private getUserCategoryAssignmentProductNameRef(userId: string): Reference {
    return this.getUserCategoryAssignmentDataRef(userId).child("productName");
  }

  private getUserCategoryAssignmentStoreNameRef(userId: string): Reference {
    return this.getUserCategoryAssignmentDataRef(userId).child("storeName");
  }

  async getCategoryAssignmentData(
    userId: string
  ): Promise<FuncResultWithData<CategoryAssignmentData>> {
    try {
      const ref = this.getUserCategoryAssignmentDataRef(userId);
      const snapshot = await ref.get();
      if (!snapshot.exists()) {
        return {
          status: FuncStatus.EMPTY,
          message: "No category assignment data found for the user.",
        };
      }

      const data = snapshot.val() as CategoryAssignmentData | null;
      if (data == null) {
        return {
          status: FuncStatus.ERROR,
          message:
            "Unable to convert Realtime Database dat to CategoryAssignmentData.",
        };
      }

      return {
        status: FuncStatus.SUCCESS,
        message: "Successfully retrieved category assignment data.",
        data: data,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to retrieve category assignment data for user ${userId}: ${error.message}`,
      };
    }
  }

  /* 基本的にfunctions側からいじらない。これはあくまでデバッグのときのみ */
  async dbgAddCategoryAssignment(userId: string) {
    const ref = this.getUserCategoryAssignmentDataRef(userId);
    try {
      await ref.set({
        productName: {},
        storeName: {
          store1: {
            id: "store1",
            name: "ローソン",
            category: "category1",
            condition: AssignmentCondition.CONTAINS,
            regex: false,
            generatedType: "rakuten_pay",
          },
        },
      });
      return {
        status: FuncStatus.SUCCESS,
        message: "Debug category assignment data added successfully.",
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to add debug category assignment data for user ${userId}: ${error.message}`,
      };
    }
  }
}
