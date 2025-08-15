package gaku.original.myapplication.viewModel.start

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncStatusInfoWithCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    init {
        Log.d(className, "Init was called")
    }

    suspend fun sendPasswordResetEmail(
        email: String,
        timeout: Long = 5000,
        callback: (FuncStatusInfoWithCode) -> Unit
    ): FuncStatusInfoWithCode {
        val statusInfo = try {
            withTimeout(timeout) {
                firebaseAuth.sendPasswordResetEmail(email).await()
                FuncStatusInfoWithCode(FuncStatus.SUCCESS, "")
            }
        } catch (e: TimeoutCancellationException) {
            FuncStatusInfoWithCode(FuncStatus.TIMEOUT, "タイムアウトしました。", null)
        } catch (e: FirebaseAuthException) {
            FuncStatusInfoWithCode(
                FuncStatus.FAILED,
                e.message ?: "Unknown error",
                e.errorCode
            )
        } catch (e: Exception) {
            FuncStatusInfoWithCode(
                FuncStatus.FAILED,
                e.message ?: "Unknown error",
                null
            )
        }

        //Dispatchers.IOでやるとクラッシュする可能性あるらしい。Mainで行う
        withContext(Dispatchers.Main) {
            callback(statusInfo)
        }
        return statusInfo
    }

    fun sendPasswordResetEmailWithCallback(
        email: String,
        timeout: Long = 5000,
        callback: (FuncStatusInfoWithCode) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            sendPasswordResetEmail(email, timeout, callback)
        }
    }
}