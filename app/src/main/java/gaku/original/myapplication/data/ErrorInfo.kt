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

    sealed class Failure : FetchResult<Nothing>() {
        abstract val errorMessage: String
        open val errorCode: String? = null

        data class GenericFailure(
            val status: SuspendFuncStatus,
            override val errorMessage: String,
            override val errorCode: String? = null
        ) : Failure()

        data class Timeout(
            override val errorMessage: String = "タイムアウトしました",
            override val errorCode: String? = "TIMEOUT"
        ) : Failure()

        // 必要に応じて他のケースを追加
    }

    fun toSuspendFuncStatusInfo(): SuspendFuncStatusInfo = when (this) {
        is Success -> SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, "")
        is Failure.GenericFailure -> SuspendFuncStatusInfo(status, errorMessage)
        is Failure.Timeout -> SuspendFuncStatusInfo(SuspendFuncStatus.TIMEOUT, errorMessage)
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