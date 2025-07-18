package gaku.original.myapplication.repository

import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun getIdToken(
        timeout: Long = 3000
    ): FetchResult<String> {
        val user = firebaseAuth.currentUser
        if (user == null) {
            val result = FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "User is null"
            )
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
            val result = FetchResult.Success(
                data = token
            )
            result
        } catch (e: TimeoutCancellationException) {
            FetchResult.Failure.Timeout()
        } catch (e: Exception) {
            val result = FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
            result
        }
    }

}