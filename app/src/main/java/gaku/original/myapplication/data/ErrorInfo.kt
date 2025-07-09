package gaku.original.myapplication.data

import android.os.Parcelable
import gaku.original.myapplication.data.Constants.Status.CheckStatus
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

sealed class FetchResult<out T> {
    data class Success<out T>(val data: T) : FetchResult<T>()
    data class Failure(
        val status: SuspendFuncStatus,
        val errorMessage: String
    ) : FetchResult<Nothing>()

    fun toSuspendFuncStatusInfo(): SuspendFuncStatusInfo = when (this) {
        is Success -> SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, "")
        is Failure -> SuspendFuncStatusInfo(status, errorMessage)
    }
}

data class SuspendFuncStatusInfoWithCode(
    val status: SuspendFuncStatus,
    val errorMessage: String,
    val errorCode: String? = null
)

data class CheckResult(
    val status: CheckStatus,
    val errorMessage: String,
    val code: String? = null,
)