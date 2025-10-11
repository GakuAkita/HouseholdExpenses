import { FuncResultWithData, FuncStatus } from "../../../type/FuncStatus";
import { BaseGoogleOAuthConfig } from "../../../type/GoogleOAuthSecrets";
import { GmailApiClient } from "../../Client/GmailApiClient";
import { loadGoogleOAuthSecrets } from "../../googleOAuthSecrets";
import { MailboxExtractionService } from "./../../RealtimeDbService/MailboxExtractionService";

export async function generateGmailApiInstance(
  userId: string,
  mailboxExtractionService: MailboxExtractionService
): Promise<FuncResultWithData<GmailApiClient>> {
  /**
   * Google認証に必要な情報+暗号化キーをロードする
   */
  /**
   * Google認証に必要な情報+暗号化キーをロードする
   */
  const secretsRet = await loadGoogleOAuthSecrets();
  if (secretsRet.status != FuncStatus.SUCCESS) {
    /**
     *  7 PERMISSION_DENIED: Permission 'secretmanager.versions.access' denied for resource .....
     * こちらのエラーが出た場合は、compute....にSecret ManagerのSecret Accessorの権限を付与する必要がある。
     * */
    return {
      status: secretsRet.status,
      message: `generateGmailApiInstance: ${secretsRet.message}`,
    };
  }

  if (!secretsRet.data) {
    return {
      status: FuncStatus.ERROR,
      message: "OAuthSecrets was done, but data was empty",
    };
  }

  const oauthSecrets = secretsRet.data;
  const encryptionKey: string = oauthSecrets.encryptionKey;

  /**
   * RealtimeDBにrefreshTokenがあるかチェックする
   * なければ、そこで終了(楽天pay設定)
   */

  const tokenRet =
    await mailboxExtractionService.getMailboxExtractionGmailTokenWithDecryption(
      userId,
      encryptionKey
    );

  if (tokenRet.status == FuncStatus.EMPTY) {
    /* まだユーザーがGmailのトークンの設定をしていない */
    return {
      status: FuncStatus.SUCCESS,
      message: "Gmail Token is not set by the user",
    };
  } else if (tokenRet.status != FuncStatus.SUCCESS) {
    return {
      status: tokenRet.status,
      message: tokenRet.message,
    };
  } else {
    /* do nothing */
  }

  if (!tokenRet.data) {
    return {
      status: FuncStatus.ERROR,
      message: "token was taken from Realtime Database, but data was empty",
    };
  }
  const refreshToken = tokenRet.data?.refreshToken;
  /* GmailApiClientを生成するのに必要なconfig */
  const gmailConfig: BaseGoogleOAuthConfig = {
    clientId: oauthSecrets.clientId,
    clientSecret: oauthSecrets.clientSecret,
    refreshToken: refreshToken,
  };

  /**
   * configをもとにGmailApiClientのインスタンス作成
   */
  const gmailApi = new GmailApiClient(gmailConfig);

  return {
    status: FuncStatus.SUCCESS,
    message: "Gmail Api Client was generated.",
    data: gmailApi,
  };
}
