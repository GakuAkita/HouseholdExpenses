import { GmailClientFactory } from "../infra/gmail/GmailApiClient";
import { GoogleOAuthApi } from "../infra/gmail/GoogleOAuthApi";
import { GoogleOAuthSecretProvider } from "../infra/secrets/googleOAuthSecrets";
import { Clock } from "./clock";
import { RuntimeConfig } from "./runtimeConfig";

/** Things from the outside world that jobs need besides the databases. */
export interface Runtime {
  clock: Clock;
  config: RuntimeConfig;
  secrets: GoogleOAuthSecretProvider;
  createGmailClient: GmailClientFactory;
  googleOAuthApi: GoogleOAuthApi;
}
