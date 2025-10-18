import { Firestore } from "firebase-admin/firestore";
import {
  FuncResult,
  FuncResultWithData,
  FuncStatus,
} from "../../type/FuncStatus";
import { RepeatAdd } from "../../type/RepeatAdd";

export class RepeatAddService {
  private db: Firestore;

  constructor(db: Firestore) {
    this.db = db;
  }

  private getUserRepeatAddColRef(userId: string) {
    return this.db.collection("users").doc(userId).collection("repeat_add");
  }

  async updateRepeatAdd(
    userId: string,
    repeatAddData: RepeatAdd
  ): Promise<FuncResult> {
    if (!repeatAddData.id)
      return {
        status: FuncStatus.ERROR,
        message: "RepeatAdd ID is required for update.",
      };
    const repeatAddRef = this.getUserRepeatAddColRef(userId).doc(
      repeatAddData.id
    );
    try {
      const docSnapshot = await repeatAddRef.get();
      if (!docSnapshot.exists)
        return {
          status: FuncStatus.ERROR,
          message: `RepeatAdd with ID ${repeatAddData.id} does not exist.`,
        };
      await repeatAddRef.set(repeatAddData);
      return {
        status: FuncStatus.SUCCESS,
        message: `RepeatAdd with ID ${repeatAddData.id} updated successfully.`,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to update RepeatAdd: ${error.message}`,
      };
    }
  }

  async addRepeatAddWithId(
    userId: string,
    repeatAddData: RepeatAdd
  ): Promise<FuncResult> {
    const repeatAddRef = this.getUserRepeatAddColRef(userId);
    const newDocRef = repeatAddRef.doc();
    repeatAddData.id = newDocRef.id;

    try {
      await newDocRef.set(repeatAddData);
      return {
        status: FuncStatus.SUCCESS,
        message: `RepeatAdd with ID ${repeatAddData.id} was added successfully.`,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to update RepeatAdd: ${error.message}`,
      };
    }
  }

  async getAllRepeatAdds(
    userId: string
  ): Promise<FuncResultWithData<Record<string, RepeatAdd>>> {
    try {
      const repeatAddRef = this.getUserRepeatAddColRef(userId);
      const snapshot = await repeatAddRef.get();

      const repeatAddMap: Record<string, RepeatAdd> = {};

      if (snapshot.empty) {
        return {
          status: FuncStatus.SUCCESS,
          message: "No RepeatAdds found.",
          data: {},
        };
      }

      snapshot.forEach((doc) => {
        const data = doc.data() as RepeatAdd;
        repeatAddMap[doc.id] = data;
      });

      return {
        status: FuncStatus.SUCCESS,
        data: repeatAddMap,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: error.message || "Failed to get RepeatAdds",
      };
    }
  }
}
