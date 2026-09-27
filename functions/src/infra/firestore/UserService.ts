import { Firestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { UserData } from "../../type/UserData";

export class UserService {
  constructor(private db: Firestore) {}

  private getUsersColRef() {
    return this.db.collection("users");
  }

  async setUserData(userId: string, data: UserData, merge: boolean = true): Promise<void> {
    await this.getUsersColRef().doc(userId).set(data, { merge });
  }

  async getAllUserIds(): Promise<string[]> {
    const snapshot = await this.getUsersColRef().get();
    logger.log("snapshot size:", snapshot.size);
    return snapshot.docs.map((doc) => doc.id);
  }
}
