import { Firestore } from "firebase-admin/firestore";
import { convertDaysToNums } from "../../constants/DayOfWeek";
import { RepeatAdd } from "../../type/RepeatAdd";

export class RepeatAddService {
  constructor(private db: Firestore) {}

  private getUserRepeatAddColRef(userId: string) {
    return this.db.collection("users").doc(userId).collection("repeat_add");
  }

  /** The user's RepeatAdds by document id. Empty when the user has none. */
  async getAllRepeatAdds(userId: string): Promise<Record<string, RepeatAdd>> {
    const snapshot = await this.getUserRepeatAddColRef(userId).get();
    const repeatAdds: Record<string, RepeatAdd> = {};
    snapshot.forEach((doc) => {
      const data = doc.data() as RepeatAdd;
      /* The app saves dayOfWeek as names (Kotlin DayOfWeek, e.g. "MONDAY"). Convert them to JS day numbers. */
      const rawDays: unknown = data.frequencyInfo?.dayOfWeek;
      if (Array.isArray(rawDays)) {
        data.frequencyInfo.dayOfWeek = convertDaysToNums(rawDays);
      }
      repeatAdds[doc.id] = data;
    });
    return repeatAdds;
  }
}
