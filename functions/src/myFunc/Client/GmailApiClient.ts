import axios from "axios";
import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { BaseGoogleOAuthConfig } from "../../type/GoogleOAuthSecrets";

type GmailMessageSearchParams = {
  q?: string;
  maxResults?: number;
  labelIds?: string[];
  includeSpamTrash?: boolean;
  pageToken?: string;
};

export class GmailApiClient {
  private accessToken: string | null = null;
  constructor(private baseConfig: BaseGoogleOAuthConfig) {}
  /**
   * refresh tokenを使用して
   * Google APIのアクセストークンを取得する
   */
  async authorize(): Promise<FuncResultWithData<string>> {
    if (this.accessToken) {
      return {
        status: FuncStatus.SUCCESS,
        message: "Using cached access token.",
        data: this.accessToken,
      };
    }

    try {
      const response = await axios.post(
        "https://oauth2.googleapis.com/token",
        null,
        {
          params: {
            client_id: this.baseConfig.clientId,
            client_secret: this.baseConfig.clientSecret,
            refreshToken: this.baseConfig.refreshToken,
            grant_type: "refresh_token",
          },
        }
      );
      if (!response.data || !response.data.access_token) {
        throw new Error(`No access token in response.`);
      }

      const token = response.data.access_token;
      if (!token) throw new Error("Access token not found in response.");

      this.accessToken = token;
      return {
        status: FuncStatus.SUCCESS,
        message: `Successfully retrieved access token.`,
        data: token,
      };
    } catch (error: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to retrieve access token: ${error.message}`,
      };
    }
  }

  /**
   * 条件に合うMailを取ってくる(idのみ)
   */
  async searchMessages(
    params: GmailMessageSearchParams
  ): Promise<FuncResultWithData<string[]>> {
    const auth = await this.authorize();
    if (auth.status != FuncStatus.SUCCESS || !auth.data) {
      return {
        status: auth.status,
        message: auth.message,
      };
    }
    try {
      const res = await axios.get(
        "https://gmail.googleapis.com/gmail/v1/users/me/messages",
        {
          headers: {
            Authorization: `Bearer ${auth.data}`,
          },
          params, // そのまま渡せる
        }
      );

      const messages = res.data.messages as { id: string }[] | undefined;
      if (messages === undefined) {
        return {
          status: FuncStatus.ERROR,
          message: "Unable to convert to ids",
        };
      }

      const msgIds = messages?.map((msg) => msg.id) ?? [];
      return {
        status: FuncStatus.SUCCESS,
        message: "Successfully get message ids",
        data: msgIds,
      };
    } catch (e: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to search mails:${e.message}`,
      };
    }
  }

  /**
   * searchMessagesをラップする
   */
  async queryMessages(
    query: string,
    maxResults: number = 5
  ): Promise<FuncResultWithData<string[]>> {
    const params: GmailMessageSearchParams = {
      q: query,
      maxResults: maxResults,
    };

    return this.searchMessages(params);
  }

  /**
   * 単一のメッセージの詳細を取得する
   */
  async getMessageDetail(messageId: string): Promise<FuncResultWithData<any>> {
    const auth = await this.authorize();
    if (auth.status !== FuncStatus.SUCCESS || !auth.data) {
      return {
        status: auth.status,
        message: auth.message,
      };
    }
    try {
      const res = await axios.get(
        `https://gmail.googleapis.com/gmail/v1/users/me/messages/${messageId}`,
        {
          headers: {
            Authorization: `Bearer ${auth.data}`,
          },
          params: {
            format: "full", // "metadata" や "raw" にも変更可能
          },
        }
      );

      return {
        status: FuncStatus.SUCCESS,
        message: "Successfully fetched message detail.",
        data: res.data,
      };
    } catch (e: any) {
      return {
        status: FuncStatus.ERROR,
        message: `Failed to fetch message detail: ${e.message}`,
      };
    }
  }
}
