package gaku.original.myapplication.viewModel.start

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.SignOutResult
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.SuspendFuncStatusInfoWithCode
import gaku.original.myapplication.viewModel.ExpenseSharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

    val isEmailVerified: Boolean?
        get() = firebaseAuth.currentUser?.isEmailVerified

    val userId: String?
        get() = firebaseAuth.currentUser?.uid

    val email: String?
        get() = firebaseAuth.currentUser?.email

    suspend fun signIn(
        email: String,
        password: String,
        isEmailVerification: Boolean = true,
        callback: (SuspendFuncStatusInfoWithCode) -> Unit
    ): SuspendFuncStatusInfoWithCode {
        return try {
            withTimeout(10000) {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                if (firebaseAuth.currentUser == null ||
                    firebaseAuth.currentUser?.uid.isNullOrEmpty()
                ) {
                    /* まず起こり得ないが、uidが入っているかチェック */
                    val statusInfo = SuspendFuncStatusInfoWithCode(
                        status = SuspendFuncStatus.FAILED,
                        errorMessage = "ユーザーIDが取得できませんでした"
                    )
                    signOut()//サインアウトしなくてもメイン画面にはいけないと思うが。念の為
                    callback(statusInfo)
                    return@withTimeout statusInfo
                }

                if (
                    firebaseAuth.currentUser?.isEmailVerified == false &&
                    isEmailVerification
                ) {
                    val statusInfo = SuspendFuncStatusInfoWithCode(
                        status = SuspendFuncStatus.FAILED,
                        errorCode = "_EMAIL_NOT_VERIFIED",
                        errorMessage = "Emailが認証されていません。認証メールを再送します。"
                    )
                    signOut()
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

    suspend fun signUp(
        email: String,
        password: String,
        isSendEmailVerification: Boolean = true,
        callback: (SuspendFuncStatusInfoWithCode) -> Unit
    ): SuspendFuncStatusInfoWithCode {
        return try {
            withTimeout(10000) {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                if (firebaseAuth.currentUser == null ||
                    firebaseAuth.currentUser?.uid.isNullOrEmpty()
                ) {
                    /* まず起こり得ないが、uidが入っているかチェック */
                    val statusInfo = SuspendFuncStatusInfoWithCode(
                        status = SuspendFuncStatus.FAILED,
                        errorMessage = "ユーザーIDが取得できませんでした"
                    )
                    signOut()//サインアウトしなくてもメイン画面にはいけないと思うが。念の為
                    callback(statusInfo)
                    return@withTimeout statusInfo
                }

                if (isSendEmailVerification) {
                    firebaseAuth.currentUser?.sendEmailVerification()
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
                _currentUser.value = null
                return SignOutResult.SUCCESS
            } else {
                //サイン・アウトしてからすぐだとここに来てしまうが、時間経って結局nullになる。
                //したがって、扱いとしてはSUCCESSにする
                Log.d(
                    "AuthManagerViewModel",
                    "!!Warning!!signOut failed. currentUser is not null\n"
                )
                _currentUser.value = null
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
        isEmailVerification: Boolean = true,
        callback: (SuspendFuncStatusInfoWithCode) -> Unit
    ) {
        viewModelScope.launch {
            signIn(email, password, isEmailVerification, callback = callback)
        }
    }

    fun signUpWithCallback(
        email: String,
        password: String,
        isSendEmailVerification: Boolean = true,
        callback: (SuspendFuncStatusInfoWithCode) -> Unit
    ) {
        viewModelScope.launch {
            val signUpStatus =
                signUp(
                    email,
                    password,
                    isSendEmailVerification = isSendEmailVerification,
                    callback = {})
            if (signUpStatus.status != SuspendFuncStatus.SUCCESS) {
                callback(signUpStatus)
                return@launch
            }

            //成功したときのみ
            expenseSharedViewModel.addInitialCategories(callback = {})
            callback(signUpStatus)
        }
    }

    /**
     * @FIXME isSignedInと統一したほうがいいかもな？
     */
    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    fun signInWithGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = withContext(Dispatchers.IO) {
                    firebaseAuth.signInWithCredential(credential).await()
                }
                _currentUser.value = result.user
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign-in failed", e)
                _currentUser.value = null
            }
        }
    }
}