import * as admin from "firebase-admin";
import { Clock, systemClock } from "../shared/clock";
import { Runtime } from "../shared/runtime";
import { readRuntimeConfig, RuntimeConfig } from "../shared/runtimeConfig";
import { initMyFirebaseAdmin } from "./firebaseAdmin";
import { CategoryService } from "./FirestoreService/CategoryService";
import { ExpenseService } from "./FirestoreService/ExpenseService";
import { RepeatAddService } from "./FirestoreService/RepeatAddService";
import { SettingsService } from "./FirestoreService/SettingsService";
import { UserService } from "./FirestoreService/UserService";
import { createGoogleOAuthSecretProvider, GoogleOAuthSecretProvider } from "./googleOAuthSecrets";
import { RepeatAddProcessor } from "./Processor/RepeatAddProcessor";
import { UserSettingsProcessor } from "./Processor/UserSettingsProcessor";
import { CategoryAssignmentService } from "./RealtimeDbService/CategoryAssignmentService";
import { MailboxExtractionService } from "./RealtimeDbService/MailboxExtractionService";
import { UserRTDbService } from "./RealtimeDbService/UserRTDbService";

export interface ServiceOptions {
  /** Passed to admin.initializeApp(). Needed for the emulator (projectId, databaseURL). */
  appOptions?: admin.AppOptions;
  clock?: Clock;
  config?: RuntimeConfig;
  secrets?: GoogleOAuthSecretProvider;
}

/**
 * Creates every service. Nothing here runs when the module is imported;
 * firebase-admin is initialized when this function is called.
 */
export const initializeServices = (options: ServiceOptions = {}) => {
  const config = options.config ?? readRuntimeConfig();
  const runtime: Runtime = {
    clock: options.clock ?? systemClock,
    config,
    secrets: options.secrets ?? createGoogleOAuthSecretProvider(config),
  };

  initMyFirebaseAdmin(options.appOptions);
  const firestoreDb = admin.firestore();
  const realtimeDb = admin.database();
  const auth = admin.auth();

  const userService = new UserService(firestoreDb);
  const expenseService = new ExpenseService(firestoreDb);
  const categoryService = new CategoryService(firestoreDb);
  const repeatAddService = new RepeatAddService(firestoreDb);
  const settingsService = new SettingsService(firestoreDb);

  /* Realtime DatabaseのService */
  const userRTDbService = new UserRTDbService(realtimeDb);
  const mailboxExtractionService = new MailboxExtractionService(realtimeDb, {
    auth,
    clock: runtime.clock,
    config,
  });
  const categoryAssignmentService = new CategoryAssignmentService(realtimeDb);

  /* Processor類 */
  const repeatAddProcessor = new RepeatAddProcessor(
    repeatAddService,
    expenseService,
    settingsService,
    runtime.clock
  );
  const userSettingsProcessor = new UserSettingsProcessor(
    userService,
    userRTDbService,
    settingsService
  );

  return {
    runtime,
    auth,
    userService,
    expenseService,
    categoryService,
    repeatAddService,
    settingsService,
    repeatAddProcessor,
    userSettingsProcessor,
    mailboxExtractionService,
    userRTDbService,
    categoryAssignmentService,
  };
};

export type Services = ReturnType<typeof initializeServices>;
