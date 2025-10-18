// ベースの型（refreshTokenあり）
export interface BaseGoogleOAuthConfig {
  clientId: string;
  clientSecret: string;
  refreshToken: string;
}

// refreshToken を除き、redirectUri と encryptionKey を追加
export interface GoogleOAuthSecrets
  extends Omit<BaseGoogleOAuthConfig, "refreshToken"> {
  redirectUri: string;
  encryptionKey: string;
}
