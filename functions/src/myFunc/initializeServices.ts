import { ExpenseService } from "./FirestoreService/ExpenseService";
import { FirestoreService } from "./FirestoreService/FirestoreService";
import { RepeatAddService } from "./FirestoreService/RepeatAddService";
import { UserService } from "./FirestoreService/UserService";
import { RepeatAddProcessor } from "./Processor/RepeatAddProcessor";

export const initializeServices = () => {
  const fsService = new FirestoreService();
  const firestoreDb = fsService.getDb();

  const userService = new UserService(firestoreDb);
  const expenseService = new ExpenseService(firestoreDb);
  const repeatAddService = new RepeatAddService(firestoreDb);
  const repeatAddProcessor = new RepeatAddProcessor(
    repeatAddService,
    expenseService
  );

  return {
    userService,
    expenseService,
    repeatAddService,
    repeatAddProcessor,
  };
};
