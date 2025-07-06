export interface MailboxTokenType {
  refreshToken: string /* 暗号化されている */;
  timestamp?: string /* ISO 8601形式のタイムスタンプ */;
}

/**
 * メールタイプの設定系
 */
export type RakutenPaySetting = {
  enabled: boolean;
  readonly nodeName: "rakuten_pay";
  readonly menuName: "楽天Pay";
  storeCategoryAssignments?: Record<string, string>;
};
export type AmazonKindle = {
  enabled: boolean;
  readonly nodeName: "amazon_kindle";
  readonly menuName: "Amazopn Kindle";
  categoryId?: string;
};

/* ここに全ての型を含めておく */
export type AllMailType = RakutenPaySetting | AmazonKindle;

/**
 * 楽天Pay設定を生成する
 */
export function createRakutenPaySettingInstance(
  params: {
    enabled: boolean;
    storeCategoryAssignments?: Record<string, string>;
  } = { enabled: true }
): RakutenPaySetting {
  return {
    nodeName: "rakuten_pay",
    menuName: "楽天Pay",
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
  } = { enabled: true }
): AmazonKindle {
  return {
    nodeName: "amazon_kindle",
    menuName: "Amazopn Kindle",
    ...params,
  };
}

/**
 * Functions側で使う、保持しておきたい情報
 */
export interface LastMailboxExtractionExec {
  timestamp: number;
  lastMsgId?: string;
}
