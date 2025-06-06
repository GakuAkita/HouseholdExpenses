package gaku.original.myapplication.data

import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus

data class ErrorInfo(
    val isError: Boolean,
    val errorMessage: String
)

data class SuspendFuncStatusInfo(
    val status: SuspendFuncStatus,
    val errorMessage: String,
)

data class FetchResult<T>(
    val status: SuspendFuncStatus,
    val errorMessage: String,
    val data: T? = null
) {
    fun toSuspendFuncStatusInfo(): SuspendFuncStatusInfo {
        return SuspendFuncStatusInfo(status, errorMessage)
    }
}

data class SuspendFuncStatusInfoWithCode(
    val status: SuspendFuncStatus,
    val errorMessage: String,
    val errorCode: String? = null
)