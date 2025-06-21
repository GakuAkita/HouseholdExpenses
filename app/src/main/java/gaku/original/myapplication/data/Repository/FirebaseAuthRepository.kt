package gaku.original.myapplication.data.Repository

import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun getIdToken(
        timeout: Long = 3000,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): FetchResult<String> {
        val user = firebaseAuth.currentUser
        if (user == null) {
            val result = FetchResult<String>(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "User is null"
            )
            callback(result.toSuspendFuncStatusInfo())
            return result
        }

        return try {
            val token = withTimeout(timeout) {
                val idTokenResult = user.getIdToken(true).await()
                val token = idTokenResult.token
                if (token.isNullOrEmpty()) {
                    throw Exception("ID Token is null or empty")
                }
                token
            }
            val result = FetchResult(
                status = SuspendFuncStatus.SUCCESS,
                errorMessage = "",
                data = token
            )
            callback(result.toSuspendFuncStatusInfo())
            result
        } catch (e: TimeoutCancellationException) {
            val result = FetchResult<String>(
                status = SuspendFuncStatus.TIMEOUT,
                errorMessage = "タイムアウトしました。idTokenの取得に失敗しました。"
            )
            callback(result.toSuspendFuncStatusInfo())
            result
        } catch (e: Exception) {
            val result = FetchResult<String>(
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
            callback(result.toSuspendFuncStatusInfo())
            result
        }
    }

}