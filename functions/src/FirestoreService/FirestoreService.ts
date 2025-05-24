// firestoreService.ts
import * as admin from "firebase-admin";
import { Firestore } from "firebase-admin/firestore";
import { initMyFirebaseAdmin } from "../myFunc/firebaseAdmin";

export class FirestoreService {
  private db: Firestore;

  constructor(options?: admin.AppOptions) {
    initMyFirebaseAdmin(options); // オプションがあればローカル
    this.db = require("firebase-admin").firestore();
  }

  get usersColRef() {
    return this.db.collection("users");
  }

  getUserDocRef(userId: string) {
    return this.usersColRef.doc(userId);
  }

  getUserExpensesColRef(userId: string) {
    return this.getUserDocRef(userId).collection("expenses");
  }

  getUserRepeatAddColRef(userId: string) {
    return this.getUserDocRef(userId).collection("repeatAdd");
  }

  getUserCategoriesColRef(userId: string) {
    return this.getUserDocRef(userId).collection("categories");
  }
}
