import axios from "axios";
import * as qs from "querystring";
import { GoogleOAuthSecrets } from "../../type/GoogleOAuthSecrets";

export interface GoogleTokens {
  accessToken: string | undefined;
  refreshToken: string | undefined;
}

/** The Google OAuth calls made when the user allows Gmail access. Tests pass a fake. */
export interface GoogleOAuthApi {
  /** Exchanges the authorization code for an access token and a refresh token. */
  exchangeCode(code: string, secrets: GoogleOAuthSecrets): Promise<GoogleTokens>;
  /** The email address of the Google account the access token belongs to. */
  fetchEmail(accessToken: string): Promise<string | undefined>;
}

export const googleOAuthApi: GoogleOAuthApi = {
  async exchangeCode(code, secrets) {
    const postData = qs.stringify({
      code,
      client_id: secrets.clientId,
      client_secret: secrets.clientSecret,
      redirect_uri: secrets.redirectUri, //uriが正しいらしい。でもsecretのほうにはurlで保存してしまった。
      grant_type: "authorization_code",
    });
    const response = await axios.post("https://oauth2.googleapis.com/token", postData, {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
    });
    const { access_token, refresh_token } = response.data;
    return { accessToken: access_token, refreshToken: refresh_token };
  },

  async fetchEmail(accessToken) {
    const response = await axios.get("https://www.googleapis.com/oauth2/v3/userinfo", {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });
    return response.data.email;
  },
};
