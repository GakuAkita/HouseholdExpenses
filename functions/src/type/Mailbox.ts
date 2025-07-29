export interface MailboxGmailTokenType {
  refreshToken: string /* 暗号化されている */;
  timestamp?: string /* ISO 8601形式のタイムスタンプ */;
  gmail: string /* Gmailのメールアドレス */;
}

/**
 * Kotlin側と合わせる
 */
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
export type ShikokuElectricPowerSetting = {
  enabled: boolean;
  emailProvider: string;
  readonly nodeName: "shikoku_electric_power";
  readonly menuName: "四国電力";
  categoryId?: string;
  readonly categoryAssignFlag: CategoryAssignFlags.NONE;
};
export type AmazonItemSetting = {
  enabled: boolean;
  emailProvider: string;
  readonly nodeName: "amazon_item";
  readonly menuName: "Amazon 物";
  readonly categoryAssignFlag: CategoryAssignFlags.PRODUCT_NAME;
};

/* ここに全ての型を含めておく */
export type AllMailType =
  | RakutenPaySetting
  | AmazonKindleSetting
  | ShikokuElectricPowerSetting
  | AmazonItemSetting;

export const allMailTypeList: AllMailType[] = [
  createRakutenPaySettingInstance(), //ここがamazon_kindleになってた、、、
  createAmazonKindleSettingInstance(),
  createShikokuElectricPowerSettingInstance(),
  createAmazonItemSettingInstance(),
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
 * ShikokuElectricPower設定を生成する
 */
export function createShikokuElectricPowerSettingInstance(
  params: {
    enabled: boolean;
    categoryId?: string;
    emailProvider: EmailProvider;
  } = {
    enabled: true,
    emailProvider: EmailProvider.GMAIL,
  }
): ShikokuElectricPowerSetting {
  return {
    nodeName: "shikoku_electric_power",
    menuName: "四国電力",
    categoryAssignFlag: CategoryAssignFlags.NONE,
    ...params,
  };
}

export function createAmazonItemSettingInstance(
  params: {
    enabled: boolean;
    emailProvider: EmailProvider;
  } = { enabled: true, emailProvider: EmailProvider.GMAIL }
): AmazonItemSetting {
  return {
    nodeName: "amazon_item",
    menuName: "Amazon 物",
    categoryAssignFlag: CategoryAssignFlags.PRODUCT_NAME,
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
