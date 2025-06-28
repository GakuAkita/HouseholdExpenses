export interface MailboxTokenType {
  refreshToken: string /* 暗号化されている */;
  timestamp?: string /* ISO 8601形式のタイムスタンプ */;
}

/**
 * メールタイプの設定系
 */
export interface RakutenPaySetting {
  enabled: boolean;
  readonly documentName: "rakuten_pay";
  readonly menuName: "楽天Pay";
  shopCategoryAssignments?: Record<string, string>;
}

export function createRakutenPaySetting(params: {
  enabled: boolean;
  shopCategoryAssignments?: Record<string, string>;
}): RakutenPaySetting {
  return {
    documentName: "rakuten_pay",
    menuName: "楽天Pay",
    ...params,
  };
}

/**
 * Functions側で使う、保持しておきたい情報
 */
export interface LastMailboxExtraction {
  timestamp: number;
  rakutenPayMsgId?: string /* 一番直近のだけ保持しておく!!! */;
  shikokuPowerMsgId?: string;
  amazonKindleMsgId?: string;
  amazonItemMsgId?: string;
}
