import { Firestore } from "firebase-admin/firestore";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";

export class UserService {
  private db: Firestore;

  constructor(db: Firestore) {
    this.db = db;
  }

  private getUsersColRef() {
    return this.db.collection("users");
  }

  async getAllUserIds(): Promise<FuncResultWithData<string[]>> {
    try {
      const snapshot = await this.getUsersColRef().get();
      const userIds: string[] = [];

      snapshot.forEach((doc) => {
        userIds.push(doc.id);
      });

      return {
        status: FuncStatus.SUCCESS,
        data: userIds,
        message: `${userIds.length} users found.`,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to get user IDs: ${error.message}`,
      };
    }
  }
}
