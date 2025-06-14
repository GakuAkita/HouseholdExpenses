package gaku.original.myapplication.data

import android.os.Parcelable
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import kotlinx.parcelize.Parcelize

data class ErrorInfo(
    val isError: Boolean,
    val errorMessage: String
)

@Parcelize
data class SuspendFuncStatusInfo(
    val status: SuspendFuncStatus,
    val errorMessage: String,
) : Parcelable

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