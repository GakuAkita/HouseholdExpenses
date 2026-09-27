import * as admin from "firebase-admin";
import { Clock, systemClock } from "../shared/clock";
import { Runtime } from "../shared/runtime";
import { readRuntimeConfig, RuntimeConfig } from "../shared/runtimeConfig";
import { createGmailApiClient, GmailClientFactory } from "../infra/gmail/GmailApiClient";
import { GoogleOAuthApi, googleOAuthApi } from "../infra/gmail/GoogleOAuthApi";
import { initMyFirebaseAdmin } from "../infra/firebaseAdmin";
import { CategoryService } from "../infra/firestore/CategoryService";
import { ExpenseService } from "../infra/firestore/ExpenseService";
import { RepeatAddService } from "../infra/firestore/RepeatAddService";
import { SettingsService } from "../infra/firestore/SettingsService";
import { UserService } from "../infra/firestore/UserService";
import { createGoogleOAuthSecretProvider, GoogleOAuthSecretProvider } from "../infra/secrets/googleOAuthSecrets";
import { RepeatAddProcessor } from "../jobs/RepeatAddProcessor";
import { UserSettingsProcessor } from "../jobs/UserSettingsProcessor";
import { CategoryAssignmentService } from "../infra/rtdb/CategoryAssignmentService";
import { MailboxExtractionService } from "../infra/rtdb/MailboxExtractionService";
import { UserRTDbService } from "../infra/rtdb/UserRTDbService";

export interface ServiceOptions {
  /** Passed to admin.initializeApp(). Needed for the emulator (projectId, databaseURL). */
  appOptions?: admin.AppOptions;
  clock?: Clock;
  config?: RuntimeConfig;
  secrets?: GoogleOAuthSecretProvider;
  createGmailClient?: GmailClientFactory;
  googleOAuthApi?: GoogleOAuthApi;
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
    createGmailClient: options.createGmailClient ?? createGmailApiClient,
    googleOAuthApi: options.googleOAuthApi ?? googleOAuthApi,
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
