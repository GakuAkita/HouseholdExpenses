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
export type AmazonSubscribeSetting = {
  enabled: Boolean;
  emailProvider: string;
  initialized: Boolean;
  readonly nodeName: "amazon_subscribe";
  readonly menuName: "Amazon定期便";
  readonly categoryAssignFlag: CategoryAssignFlags.NONE;
};

export type UdemySetting = {
  enabled: boolean;
  emailProvider: string;
  readonly nodeName: "udemy";
  readonly menuName: "Udemy";
  categoryId?: string;
  readonly categoryAssignFlag: CategoryAssignFlags.NONE;
};

/**
 * 将来的にはスクレーピングにするかな～
 * その時はこいつを消せばいいか。
 * あと、使ってから費用追加されるまで時間差があるからできれば通知をしたい
 */
export type RakutenCardETCSetting = {
  enabled: boolean;
  emailProvider: string;
  readonly nodeName: "rakuten_card_etc";
  readonly menuName: "楽天ETC";
  categoryId?: string;
  readonly categoryAssignFlag: CategoryAssignFlags.NONE;
};

/* ここに全ての型を含めておく */
export type AllMailType =
  | RakutenPaySetting
  | AmazonKindleSetting
  | AmazonSubscribeSetting
  | ShikokuElectricPowerSetting
  | AmazonItemSetting
  | UdemySetting
  | RakutenCardETCSetting;

/**
 * クロン表現
 * 実行タイプ等を紐づけておく
 * このほうがメンテナンスしやすい
 */
type MailExtractionSchedule = {
  cron: string;
  id: string; // ← 追加（任意）
  description: string;
  mailTypes: AllMailType[];
};

export const mailboxExtractionSchedules: MailExtractionSchedule[] = [
  {
    id: "shortPeriod",
    cron: "*/5 * * * *",
    description: "5分周期:楽天Pay、Amazon",
    mailTypes: [
      createRakutenPaySettingInstance(),
      createAmazonItemSettingInstance(),
      createAmazonKindleSettingInstance(),
    ],
  },
  {
    id: "daily",
    cron: "0 1 * * *",
    description: "毎日:四国電力、udemy、楽天ETCなど",
    mailTypes: [
      createShikokuElectricPowerSettingInstance(),
      createUdemySettingInstance(),
      createRakutenCardETCSettingInstance(),
    ],
  },
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

/**
 * Amazon物の設定を生成する
 */
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
 * Amazon定期便の設定を生成する
 */
export function createAmazonSubscribeSettingInstance(
  params: {
    enabled: boolean;
    emailProvider: EmailProvider;
    initialized: boolean;
  } = { enabled: true, emailProvider: EmailProvider.GMAIL, initialized: false }
): AmazonSubscribeSetting {
  return {
    nodeName: "amazon_subscribe",
    menuName: "Amazon定期便",
    categoryAssignFlag: CategoryAssignFlags.NONE,
    ...params,
  };
}

/**
 * Udemyの設定を生成する
 */
export function createUdemySettingInstance(
  params: {
    enabled: boolean;
    categoryId?: string;
    emailProvider: EmailProvider;
  } = {
    enabled: true,
    emailProvider: EmailProvider.GMAIL,
  }
): UdemySetting {
  return {
    nodeName: "udemy",
    menuName: "Udemy",
    categoryAssignFlag: CategoryAssignFlags.NONE,
    ...params,
  };
}

/**
 * 楽天カードETCの設定を生成する
 */
export function createRakutenCardETCSettingInstance(
  params: {
    enabled: boolean;
    categoryId?: string;
    emailProvider: EmailProvider;
  } = {
    enabled: true,
    emailProvider: EmailProvider.GMAIL,
  }
): RakutenCardETCSetting {
  return {
    nodeName: "rakuten_card_etc",
    menuName: "楽天ETC",
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

export interface AmazonSubscribeItem {
  id?: string;
  productName?: string;
  quantity?: number;
  price?: number;
  timestamp: number;
}
