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
  shopCategoryAssignments?: Record<string, string>;
};
export type AmazonKindle = {
  enabled: boolean;
  readonly nodeName: "amazon_kindle";
  readonly menuName: "Amazopn Kindle";
};

/* ここに全ての型を含めておく */
export type AllMailType = RakutenPaySetting | AmazonKindle;

export function createRakutenPaySettingInstance(params: {
  enabled: boolean;
  shopCategoryAssignments?: Record<string, string>;
}): RakutenPaySetting {
  return {
    nodeName: "rakuten_pay",
    menuName: "楽天Pay",
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
