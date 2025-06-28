import { Database } from "firebase-admin/database";
import { admin, initMyFirebaseAdmin } from "../firebaseAdmin";

export class RealtimeDbService {
  private db: Database;

  constructor(options?: admin.AppOptions) {
    initMyFirebaseAdmin(options);
    this.db = admin.database();
  }

  getDb() {
    return this.db;
  }
}
