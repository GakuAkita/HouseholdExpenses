import { GmailClientFactory } from "../myFunc/Client/GmailApiClient";
import { GoogleOAuthSecretProvider } from "../myFunc/googleOAuthSecrets";
import { Clock } from "./clock";
import { RuntimeConfig } from "./runtimeConfig";

/** Things from the outside world that jobs need besides the databases. */
export interface Runtime {
  clock: Clock;
  config: RuntimeConfig;
  secrets: GoogleOAuthSecretProvider;
  createGmailClient: GmailClientFactory;
}
