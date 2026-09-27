import axios from "axios";
import { logger } from "firebase-functions";
import { gmail_v1 } from "googleapis";
import { BaseGoogleOAuthConfig } from "../../type/GoogleOAuthSecrets";

/** Calls the Gmail REST API with an access token from the refresh token. Methods throw on failure. */
export class GmailApiClient {
  private accessToken: string | null = null;
  constructor(private baseConfig: BaseGoogleOAuthConfig) {}

  /** Gets an access token with the refresh token, once per client. */
  async authorize(): Promise<string> {
    if (this.accessToken) {
      return this.accessToken;
    }
    try {
      const response = await axios.post("https://oauth2.googleapis.com/token", null, {
        params: {
          client_id: this.baseConfig.clientId,
          client_secret: this.baseConfig.clientSecret,
          refreshToken: this.baseConfig.refreshToken,
          grant_type: "refresh_token",
        },
      });
      const token = response.data?.access_token;
      if (!token) throw new Error("Access token not found in response.");
      this.accessToken = token;
      return token;
    } catch (error: any) {
      throw new Error(`Failed to retrieve access token: ${error.message}`);
    }
  }

  /** Ids of the mails that match the Gmail search query. */
  async queryMessages(query: string, maxResults: number = 5): Promise<string[]> {
    const accessToken = await this.authorize();
    try {
      const res = await axios.get("https://gmail.googleapis.com/gmail/v1/users/me/messages", {
        headers: { Authorization: `Bearer ${accessToken}` },
        params: { q: query, maxResults },
      });
      const messages = res.data.messages as { id: string }[] | undefined;
      return messages?.map((msg) => msg.id) ?? [];
    } catch (e: any) {
      logger.error("searchMessages error:", e.response?.data ?? e.message ?? e);
      throw new Error(`Failed to search mails:${e.message}`);
    }
  }

  /** 単一のメッセージの詳細を取得する */
  async getMessageDetail(messageId: string): Promise<gmail_v1.Schema$Message> {
    const accessToken = await this.authorize();
    try {
      const res = await axios.get<gmail_v1.Schema$Message>(
        `https://gmail.googleapis.com/gmail/v1/users/me/messages/${messageId}`,
        {
          headers: { Authorization: `Bearer ${accessToken}` },
          params: { format: "full" }, // "metadata" や "raw" にも変更可能
        }
      );
      return res.data;
    } catch (e: any) {
      throw new Error(`Failed to fetch message detail: ${e.message}`);
    }
  }
}

/** The part of the Gmail API the jobs use. Tests pass a fake that returns fixture emails. */
export type GmailClient = Pick<GmailApiClient, "queryMessages" | "getMessageDetail">;

export type GmailClientFactory = (config: BaseGoogleOAuthConfig) => GmailClient;

export const createGmailApiClient: GmailClientFactory = (config) => new GmailApiClient(config);
