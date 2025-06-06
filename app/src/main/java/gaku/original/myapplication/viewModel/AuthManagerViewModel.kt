package gaku.original.myapplication.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.SignOutResult
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.SuspendFuncStatusInfoWithCode
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class AuthManagerViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val expenseSharedViewModel: ExpenseSharedViewModel
) : ViewModel() {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"
    override fun onCleared() {
        super.onCleared()
        Log.d(className, "${className}Cleared!!!!")
    }

    val isSignedIn: Boolean
        get() = firebaseAuth.currentUser != null

    val userId: String?
        get() = firebaseAuth.currentUser?.uid

    val email: String?
        get() = firebaseAuth.currentUser?.email

    suspend fun signIn(
        email: String,
        password: String,
        callback: (SuspendFuncStatusInfoWithCode) -> Unit
    ): SuspendFuncStatusInfoWithCode {
        return try {
            withTimeout(10000) {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val uid = firebaseAuth.currentUser?.uid
                if (uid.isNullOrEmpty()) {
                    /* まず起こり得ないが、uidが入っているかチェック */
                    val statusInfo = SuspendFuncStatusInfoWithCode(
                        status = SuspendFuncStatus.FAILED,
                        errorMessage = "ユーザーIDが取得できませんでした"
                    )
                    callback(statusInfo)
                    return@withTimeout statusInfo
                }

                val statusInfo = SuspendFuncStatusInfoWithCode(
                    status = SuspendFuncStatus.SUCCESS,
                    errorMessage = ""
                )
                callback(statusInfo)
                statusInfo
            }
        } catch (e: TimeoutCancellationException) {
            val statusInfo = SuspendFuncStatusInfoWithCode(
                status = SuspendFuncStatus.TIMEOUT,
                errorMessage = "タイムアウトしました"
            )
            callback(statusInfo)
            statusInfo
        } catch (e: FirebaseAuthException) {
            val statusInfo = SuspendFuncStatusInfoWithCode(
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "予期せぬエラーが発生しました",
                errorCode = e.errorCode
            )
            statusInfo
        } catch (e: Exception) {
            val statusInfo = SuspendFuncStatusInfoWithCode(
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "予期せぬエラーが発生しました"
            )
            callback(statusInfo)
            statusInfo
        }
    }

    suspend fun signUp(
        email: String,
        password: String,
        callback: (SuspendFuncStatusInfoWithCode) -> Unit
    ): SuspendFuncStatusInfoWithCode {
        return try {
            withTimeout(10000) {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val statusInfo = SuspendFuncStatusInfoWithCode(
                    status = SuspendFuncStatus.SUCCESS,
                    errorMessage = ""
                )
                callback(statusInfo)
                statusInfo
            }
        } catch (e: TimeoutCancellationException) {
            val statusInfo = SuspendFuncStatusInfoWithCode(
                status = SuspendFuncStatus.TIMEOUT,
                errorMessage = "タイムアウトしました"
            )
            callback(statusInfo)
            statusInfo
        } catch (e: FirebaseAuthException) {
            val statusInfo = SuspendFuncStatusInfoWithCode(
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "予期せぬエラーが発生しました",
                errorCode = e.errorCode
            )
            callback(statusInfo)
            statusInfo
        } catch (e: Exception) {
            val statusInfo = SuspendFuncStatusInfoWithCode(
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "予期せぬエラーが発生しました"
            )
            callback(statusInfo)
            statusInfo
        }
    }

    fun signOut(): SignOutResult {
        try {
            expenseSharedViewModel.onSignedOut()

            firebaseAuth.signOut()
            if (userId == null) {
                Log.d("AuthManagerViewModel", "signOut successful")
                return SignOutResult.SUCCESS
            } else {
                //サイン・アウトしてからすぐだとここに来てしまうが、時間経って結局nullになる。
                //したがって、扱いとしてはSUCCESSにする
                Log.d(
                    "AuthManagerViewModel",
                    "!!Warning!!signOut failed. currentUser is not null\n"
                )
                return SignOutResult.SUCCESS
            }
        } catch (e: Exception) {
            Log.d("AuthManagerViewModel", "signOut failed : ${e.message}")
            return SignOutResult.SIGN_OUT_FAILED
        }
    }

    /**
     * シンプルなsignIn関数が定義してあって、
     * こっちが実際にViewで呼び出す方。
     */
    fun signInWithCallback(
        email: String,
        password: String,
        callback: (SuspendFuncStatusInfoWithCode) -> Unit
    ) {
        viewModelScope.launch {
            signIn(email, password, callback)
        }
    }

    fun signUpSignInWithCallback(
        email: String,
        password: String,
        callback: (SuspendFuncStatusInfoWithCode) -> Unit
    ) {
        viewModelScope.launch {
            val signUpStatus = signUp(email, password, callback = {})
            if (signUpStatus.status != SuspendFuncStatus.SUCCESS) {
                callback(signUpStatus)
                return@launch
            }

            val signInStatus = signIn(email, password, callback = {})
            if (signInStatus.status != SuspendFuncStatus.SUCCESS) {
                callback(signInStatus)
                return@launch
            }
            callback(signInStatus)
        }
    }

    fun onSignedUp() {

    }
}