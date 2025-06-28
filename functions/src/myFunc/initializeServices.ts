import { ExpenseService } from "./FirestoreService/ExpenseService";
import { FirestoreService } from "./FirestoreService/FirestoreService";
import { MailboxExtractionParamsService } from "./FirestoreService/MailboxExtractionParamsService";
import { RepeatAddService } from "./FirestoreService/RepeatAddService";
import { SettingsService } from "./FirestoreService/SettingsService";
import { UserService } from "./FirestoreService/UserService";
import { RepeatAddProcessor } from "./Processor/RepeatAddProcessor";
import { UserSettingsProcessor } from "./Processor/UserSettingsProcessor";
import { RealtimeDbService } from "./RealtimeDbService/RealtimeDbService";
import { UserRTDbService } from "./RealtimeDbService/UserRTDbService";

export const initializeServices = () => {
  const fsService = new FirestoreService();
  const firestoreDb = fsService.getDb();

  const userService = new UserService(firestoreDb);
  const expenseService = new ExpenseService(firestoreDb);
  const repeatAddService = new RepeatAddService(firestoreDb);
  const settingsService = new SettingsService(firestoreDb);

  /* Realtime DatabaseのService */
  const rtService = new RealtimeDbService();
  const realtimeDb = rtService.getDb();

  const userRTDbService = new UserRTDbService(realtimeDb);

  /* Processor類 */
  const repeatAddProcessor = new RepeatAddProcessor(
    repeatAddService,
    expenseService,
    settingsService
  );
  const userSettingsProcessor = new UserSettingsProcessor(
    userService,
    userRTDbService,
    settingsService
  );
  const mailboxExtractionParamsService = new MailboxExtractionParamsService(
    firestoreDb
  );

  return {
    userService,
    expenseService,
    repeatAddService,
    repeatAddProcessor,
    userSettingsProcessor,
    mailboxExtractionParamsService,
    userRTDbService,
  };
};
