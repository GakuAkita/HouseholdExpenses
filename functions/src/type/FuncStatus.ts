export enum FuncStatus {
  SUCCESS = "success",
  TIMEOUT = "timeout",
  ERROR = "error",
}

export interface FuncResult {
  status: FuncStatus;
  message?: string;
}

export interface FirestoreAddResult extends FuncResult {
  id?: string; // 成功時に追加されたドキュメントのID
}

export interface FuncResultWithData<T> extends FuncResult {
  data?: T; // 成功時に返されるデータ
}
