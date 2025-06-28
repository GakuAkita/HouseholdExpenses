import { FuncResultWithData, FuncStatus } from "../../type/FuncStatus";
import { BaseGoogleOAuthConfig } from "../../type/GoogleOAuthSecrets";
import { GmailApiClient } from "../Client/GmailApiClient";
import { loadGoogleOAuthSecrets } from "../googleOAuthSecrets";
import { MailboxExtractionMailTypeSettingsService } from "../RealtimeDbService/MailboxExtractionMailTypeSetttingsService";
import { MailboxExtractionService } from "../RealtimeDbService/MailboxExtractionService";
export class MailboxExtractionProcessor {
  constructor(
    private mailboxExtractionService: MailboxExtractionService,
    private mailboxExtractionMailTypeSettingsService: MailboxExtractionMailTypeSettingsService
  ) {}

  async generateGmailApiInstance(
    userId: string
  ): Promise<FuncResultWithData<GmailApiClient>> {
    /**
     * Google認証に必要な情報+暗号化キーをロードする
     */
    const secretsRet = await loadGoogleOAuthSecrets();
    if (secretsRet.status != FuncStatus.SUCCESS) {
      return {
        status: secretsRet.status,
        message: `rakutenPayProcess: ${secretsRet.message}`,
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
     * firestoreにrefreshTokenがあるかチェックする
     * なければ、そこで終了(楽天pay設定)
     */
    const tokenRet =
      await this.mailboxExtractionService.getMailboxExtractionTokenWithDecryption(
        userId,
        encryptionKey
      );

    if (tokenRet.status == FuncStatus.EMPTY) {
      /* まだユーザーが楽天Payの設定をしていない */
      return {
        status: FuncStatus.SUCCESS,
        message: "Rakuten Pay settting is not set by the user",
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
        message: "token was taken from Firestore, but data was empty",
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

  /* ここまでは全部どのメールを取ろうが共通だから関数化したほうがいいな。 */

  // /**
  //  * 楽天Payのデータを取得するためのクエリを定義して検索
  //  * 現在時刻と前回の時刻の間でクエリをかけたい。
  //  * そのためには前回の時刻を保存&取得してこないといけない。
  //  */
  // const rakutenQuery = "in:inbox subject:楽天ペイアプリご利用内容確認メール";
  // const msgIdsRet = await gmailApi.queryMessages(rakutenQuery);
  // if (msgIdsRet.status != FuncStatus.SUCCESS) {
  //   return {
  //     status: msgIdsRet.status,
  //     message: msgIdsRet.message,
  //   };
  // }

  // /**
  //  * 取得できたmsgIdsが直近の保存されているメッセージIDと被っていないかチェック
  //  * また、Firestoreに保存しておく一番最近のmsgIdを覚えておく
  //  */

  // /**
  //  * ヒットしたmsgIdに対して詳細を取得しデータを抽出する。
  //  * Firestoreに保存(Not categoriedとして。)
  //  */

  // /**
  //  * あとは新しいお店だったら、
  //  */

  // return {
  //   status: FuncStatus.SUCCESS,
  //   message: "Successfully processed RakutenPay.",
  // };
}
