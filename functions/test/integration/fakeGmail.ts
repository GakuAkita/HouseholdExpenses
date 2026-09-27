import { readFileSync } from "fs";
import { join } from "path";
import { GmailClient, GmailClientFactory } from "../../src/infra/gmail/GmailApiClient";
import { FuncStatus } from "../../src/type/FuncStatus";
import { BaseGoogleOAuthConfig } from "../../src/type/GoogleOAuthSecrets";

export interface FakeMail {
  id: string;
  /** Matched against the "from:" part of the Gmail query. */
  from: string;
  internalDate: Date;
  text: string;
  subject?: string;
  /** When set, the mail also has a text/html part. */
  html?: string;
}

const base64url = (text: string) => Buffer.from(text, "utf8").toString("base64url");

export const mailFixture = (name: string, extension = "txt") =>
  readFileSync(join(__dirname, "../fixtures/mail", `${name}.${extension}`), "utf8").replace(/\r\n/g, "\n");

/**
 * A Gmail that holds the given mails. A query returns the mails whose sender matches its "from:".
 * It records the config it was created with and the queries it received.
 */
export const createFakeGmail = (mails: FakeMail[]) => {
  const createdWith: BaseGoogleOAuthConfig[] = [];
  const queries: string[] = [];

  const client: GmailClient = {
    async queryMessages(query, maxResults = 5) {
      queries.push(query);
      const from = /from:(\S+)/.exec(query)?.[1];
      const ids = mails.filter((mail) => mail.from === from).map((mail) => mail.id);
      return { status: FuncStatus.SUCCESS, data: ids.slice(0, maxResults) };
    },
    async getMessageDetail(id) {
      const mail = mails.find((m) => m.id === id);
      if (!mail) return { status: FuncStatus.ERROR, message: `No mail ${id}` };
      return {
        status: FuncStatus.SUCCESS,
        data: {
          id,
          internalDate: String(mail.internalDate.getTime()),
          payload: {
            mimeType: "multipart/alternative",
            headers: mail.subject ? [{ name: "Subject", value: mail.subject }] : [],
            parts: [
              { mimeType: "text/plain", body: { data: base64url(mail.text) } },
              ...(mail.html ? [{ mimeType: "text/html", body: { data: base64url(mail.html) } }] : []),
            ],
          },
        },
      };
    },
  };

  const factory: GmailClientFactory = (config) => {
    createdWith.push(config);
    return client;
  };

  return { factory, createdWith, queries };
};
