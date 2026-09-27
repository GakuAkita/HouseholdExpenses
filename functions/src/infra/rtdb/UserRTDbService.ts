import { Database } from "firebase-admin/database";
import { UserData } from "../../type/UserData";

export class UserRTDbService {
  constructor(private db: Database) {}

  /** Merges the data into users/{userId}, keeping the user's other data. */
  async setUserData(userId: string, userData: UserData): Promise<void> {
    await this.db.ref("users").child(userId).update(userData);
  }
}
