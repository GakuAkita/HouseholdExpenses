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
    data class Success<out T>(val data: T, val isEmpty: Boolean = false) : FetchResult<T>()

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

/* 戻り値で変換したい場合は、.mapFailureだけで十分。わざわざ変数にあてなくても。 */
inline fun <T, reified T2> FetchResult<T>.mapFailure(): FetchResult<T2> = when (this) {
    is FetchResult.Success -> throw IllegalStateException("Success cannot be converted using mapFailure")
    is FetchResult.Failure -> when (this) {
        is FetchResult.Failure.GenericFailure -> FetchResult.Failure.GenericFailure(
            status = this.status,
            errorMessage = this.errorMessage,
            errorCode = this.errorCode
        )

        is FetchResult.Failure.Timeout -> FetchResult.Failure.Timeout(
            errorMessage = this.errorMessage,
            errorCode = this.errorCode
        )
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