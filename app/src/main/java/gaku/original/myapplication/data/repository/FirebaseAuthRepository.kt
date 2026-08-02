package gaku.original.myapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun getIdToken(
        timeout: Long = 3000
    ): FuncResultWithData<String> {
        val user = firebaseAuth.currentUser
        if (user == null) {
            val result = FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
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
            val result = FuncResultWithData.Success(
                data = token
            )
            result
        } catch (e: TimeoutCancellationException) {
            FuncResultWithData.Failure.Timeout()
        } catch (e: Exception) {
            val result = FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
            result
        }
    }

}