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

sealed class FuncResultWithData<out T> {
    data class Success<out T>(val data: T, val isEmpty: Boolean = false) : FuncResultWithData<T>()

    sealed class Failure : FuncResultWithData<Nothing>() {
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
inline fun <T, reified T2> FuncResultWithData<T>.mapFailure(): FuncResultWithData<T2> =
    when (this) {
        is FuncResultWithData.Success -> throw IllegalStateException("Success cannot be converted using mapFailure")
        is FuncResultWithData.Failure -> when (this) {
            is FuncResultWithData.Failure.GenericFailure -> FuncResultWithData.Failure.GenericFailure(
                status = this.status,
                errorMessage = this.errorMessage,
                errorCode = this.errorCode
            )

            is FuncResultWithData.Failure.Timeout -> FuncResultWithData.Failure.Timeout(
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