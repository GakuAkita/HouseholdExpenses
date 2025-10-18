export enum FuncStatus {
  SUCCESS = "success",
  TIMEOUT = "timeout",
  ERROR = "error",
  EMPTY = "empty",
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

export function toFuncResult<T>(result: FuncResultWithData<T>): FuncResult {
  return {
    status: result.status,
    message: result.message,
  };
}
