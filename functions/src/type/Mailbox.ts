export interface MailboxGmailTokenType {
  refreshToken: string /* 暗号化されている */;
  timestamp?: string /* ISO 8601形式のタイムスタンプ */;
  gmail: string /* Gmailのメールアドレス */;
}

export enum CategoryAssignFlags {
  NONE = 0x000,
  PRODUCT_NAME = 0x001,
  STORE_NAME = 0x002,
}

export enum EmailProvider /* 今のところgmailのみ */ {
  GMAIL = "GMAIL",
  OUTLOOK = "OUTLOOK",
  YAHOO = "YAHOO",
}

/**
 * メールタイプの設定系
 */
export type RakutenPaySetting = {
  enabled: boolean;
  emailProvider: string;
  readonly nodeName: "rakuten_pay";
  readonly menuName: "楽天Pay";
  readonly categoryAssignFlag: CategoryAssignFlags.STORE_NAME;
};
export type AmazonKindleSetting = {
  enabled: boolean;
  emailProvider: string;
  readonly nodeName: "amazon_kindle";
  readonly menuName: "Amazon Kindle";
  categoryId?: string;
  readonly categoryAssignFlag: CategoryAssignFlags.NONE;
};

/* ここに全ての型を含めておく */
export type AllMailType = RakutenPaySetting | AmazonKindleSetting;
export const allMailTypeList: AllMailType[] = [
  createRakutenPaySettingInstance(), //ここがamazon_kindleになってた、、、
  createAmazonKindleSettingInstance(),
];

/**
 * 楽天Pay設定を生成する
 */
export function createRakutenPaySettingInstance(
  params: {
    enabled: boolean;
    emailProvider: EmailProvider;
  } = { enabled: true, emailProvider: EmailProvider.GMAIL }
): RakutenPaySetting {
  return {
    nodeName: "rakuten_pay",
    menuName: "楽天Pay",
    categoryAssignFlag: CategoryAssignFlags.STORE_NAME,
    ...params,
  };
}

/**
 * Amazon Kindle設定を生成する
 */
export function createAmazonKindleSettingInstance(
  params: {
    enabled: boolean;
    categoryId?: string;
    emailProvider: EmailProvider;
  } = {
    enabled: true,
    emailProvider: EmailProvider.GMAIL,
  }
): AmazonKindleSetting {
  return {
    nodeName: "amazon_kindle",
    menuName: "Amazon Kindle",
    categoryAssignFlag: CategoryAssignFlags.NONE,
    ...params,
  };
}

/**
 * Functions側で使う、保持しておきたい情報
 */
export interface LastMailboxExtractionExec {
  timestamp: number;
  lastMsgId?: string | null;
}
