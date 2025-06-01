import { ExpenseService } from "./FirestoreService/ExpenseService";
import { FirestoreService } from "./FirestoreService/FirestoreService";
import { RepeatAddService } from "./FirestoreService/RepeatAddService";
import { SettingsService } from "./FirestoreService/SettingsService";
import { UserService } from "./FirestoreService/UserService";
import { RepeatAddProcessor } from "./Processor/RepeatAddProcessor";

export const initializeServices = () => {
  const fsService = new FirestoreService();
  const firestoreDb = fsService.getDb();

  const userService = new UserService(firestoreDb);
  const expenseService = new ExpenseService(firestoreDb);
  const repeatAddService = new RepeatAddService(firestoreDb);
  const settingsService = new SettingsService(firestoreDb);
  const repeatAddProcessor = new RepeatAddProcessor(
    repeatAddService,
    expenseService,
    settingsService
  );

  return {
    userService,
    expenseService,
    repeatAddService,
    repeatAddProcessor,
  };
};
