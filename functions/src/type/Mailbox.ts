export interface MailboxTokenType {
  refreshToken: string /* 暗号化されている */;
  timestamp?: string /* ISO 8601形式のタイムスタンプ */;
}

export type CategoryAssignment = {
  id?: string;
  categoryId: string;
  name: string;
  condition: string; // 後々入力制限する
};

/**
 * メールタイプの設定系
 */
export type RakutenPaySetting = {
  enabled: boolean;
  readonly nodeName: "rakuten_pay";
  readonly menuName: "楽天Pay";
  storeCategoryAssignments?: Record<string, CategoryAssignment>;
};
export type AmazonKindleSetting = {
  enabled: boolean;
  readonly nodeName: "amazon_kindle";
  readonly menuName: "Amazon Kindle";
  categoryId?: string;
};

/* ここに全ての型を含めておく */
export type AllMailType = RakutenPaySetting | AmazonKindleSetting;
export const allMailTypeList: AllMailType[] = [
  createAmazonKindleSettingInstance(),
  createAmazonKindleSettingInstance(),
];

/**
 * 楽天Pay設定を生成する
 */
export function createRakutenPaySettingInstance(
  params: {
    enabled: boolean;
    storeCategoryAssignments?: Record<string, CategoryAssignment>;
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
): AmazonKindleSetting {
  return {
    nodeName: "amazon_kindle",
    menuName: "Amazon Kindle",
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
