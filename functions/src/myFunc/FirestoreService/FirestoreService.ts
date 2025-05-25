import * as admin from "firebase-admin";
import { Firestore } from "firebase-admin/firestore";
import { initMyFirebaseAdmin } from "../firebaseAdmin";

export class FirestoreService {
  private db: Firestore;

  constructor(options?: admin.AppOptions) {
    initMyFirebaseAdmin(options);
    this.db = admin.firestore();
  }

  getDb() {
    return this.db;
  }
}
