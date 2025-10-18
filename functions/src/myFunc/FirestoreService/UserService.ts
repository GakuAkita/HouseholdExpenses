import { Firestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import {
  FuncResult,
  FuncResultWithData,
  FuncStatus,
} from "../../type/FuncStatus";
import { UserData } from "../../type/UserData";

export class UserService {
  private db: Firestore;

  constructor(db: Firestore) {
    this.db = db;
  }

  private getUsersColRef() {
    return this.db.collection("users");
  }

  private getUserDocRef(userId: string) {
    return this.getUsersColRef().doc(userId);
  }

  async addUserCol(userId: string): Promise<FuncResultWithData<string>> {
    try {
      await this.getUserDocRef(userId).set({});
      return {
        status: FuncStatus.SUCCESS,
        data: userId,
        message: `User ${userId} added successfully.`,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to add user ${userId}: ${error.message}`,
      };
    }
  }

  async setUserData(
    userId: string,
    data: UserData,
    merge: boolean = true
  ): Promise<FuncResult> {
    try {
      await this.getUserDocRef(userId).set(data, { merge });
      return {
        status: FuncStatus.SUCCESS,
        message: `Data for user ${userId} set successfully.`,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to set data for user ${userId}: ${error.message}`,
      };
    }
  }

  async getAllUserIds(): Promise<FuncResultWithData<string[]>> {
    try {
      const snapshot = await this.getUsersColRef().get();
      logger.log("snapshot size:", snapshot.size);
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
