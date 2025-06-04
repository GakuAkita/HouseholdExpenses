import { ExpenseService } from "./FirestoreService/ExpenseService";
import { FirestoreService } from "./FirestoreService/FirestoreService";
import { RepeatAddService } from "./FirestoreService/RepeatAddService";
import { SettingsService } from "./FirestoreService/SettingsService";
import { UserService } from "./FirestoreService/UserService";
import { RepeatAddProcessor } from "./Processor/RepeatAddProcessor";
import { UserSettingsProcessor } from "./Processor/UserSettingsProcessor";

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
  const userSettingsProcessor = new UserSettingsProcessor(
    userService,
    settingsService
  );

  return {
    userService,
    expenseService,
    repeatAddService,
    repeatAddProcessor,
    userSettingsProcessor,
  };
};
