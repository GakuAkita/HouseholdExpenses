/**
 * Values read from environment variables. They are read once, in one place,
 * and passed to the code that needs them.
 */
export interface RuntimeConfig {
  /** true when running in the Firebase emulator (FUNCTIONS_EMULATOR). */
  isEmulator: boolean;
  /** GCLOUD_PROJECT. Used for the Secret Manager path. */
  projectId: string | undefined;
  /** Emulator only: the Gmail address whose token is used (MY_GMAIL). */
  emulatorGmail: string | undefined;
  /** Emulator only: the Google OAuth secrets as JSON (GOOGLE_OAUTH_SECRETS). */
  emulatorGoogleOAuthSecrets: string | undefined;
}

export const readRuntimeConfig = (env: NodeJS.ProcessEnv = process.env): RuntimeConfig => ({
  isEmulator: env.FUNCTIONS_EMULATOR === "true",
  projectId: env.GCLOUD_PROJECT,
  emulatorGmail: env.MY_GMAIL,
  emulatorGoogleOAuthSecrets: env.GOOGLE_OAUTH_SECRETS,
});
