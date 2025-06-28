import { Database, Reference } from "firebase-admin/database";
import { FuncResult, FuncStatus } from "../../type/FuncStatus";
import { UserData } from "../../type/UserData";

export class UserRTDbService {
  private db: Database;
  constructor(private rtdb: Database) {
    this.db = rtdb;
  }

  private getUsersRef(): Reference {
    return this.db.ref("users");
  }

  private getUserRef(userId: string): Reference {
    return this.getUsersRef().child(userId);
  }

  async setUserData(userId: string, userData: UserData): Promise<FuncResult> {
    try {
      await this.getUserRef(userId).set(userData);
      return {
        status: FuncStatus.SUCCESS,
        message: "Successfully set userData",
      };
    } catch (e: any) {
      return {
        status: FuncStatus.ERROR,
        message: `${e.message}`,
      };
    }
  }
}
