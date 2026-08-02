package gaku.original.myapplication.ui.screens.start.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

data class SignInUiState(
    val isLoading: Boolean = false,
    val message:String? = null,
    val email:String = "",
    val password:String = ""
)

class SignInViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    override fun onCleared() {
        super.onCleared()
        Timber.d("Cleared!!!!${hashCode()}")
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val authRepository = app.appContainer.authRepository
                SignInViewModel(authRepository)
            }
        }
    }

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState

    init{
        Timber.d("Created!!!!${hashCode()}")
    }

    suspend fun signIn(

    ){

    }
//    suspend fun signIn(
//        email: String,
//        password: String,
//        isEmailVerification: Boolean = true,
//        callback: (FuncStatusInfoWithCode) -> Unit
//    ): FuncStatusInfoWithCode {
//        return try {
//            withTimeout(10000) {
//                firebaseAuth.signInWithEmailAndPassword(email, password).await()
//                if (firebaseAuth.currentUser == null ||
//                    firebaseAuth.currentUser?.uid.isNullOrEmpty()
//                ) {
//                    /* まず起こり得ないが、uidが入っているかチェック */
//                    val statusInfo = FuncStatusInfoWithCode(
//                        status = FuncStatus.FAILED,
//                        errorMessage = "ユーザーIDが取得できませんでした"
//                    )
//                    signOut()//サインアウトしなくてもメイン画面にはいけないと思うが。念の為
//                    callback(statusInfo)
//                    return@withTimeout statusInfo
//                }
//
//                if (
//                    firebaseAuth.currentUser?.isEmailVerified == false &&
//                    isEmailVerification
//                ) {
//                    val statusInfo = FuncStatusInfoWithCode(
//                        status = FuncStatus.FAILED,
//                        errorCode = "_EMAIL_NOT_VERIFIED",
//                        errorMessage = "Emailが認証されていません。認証メールを再送します。"
//                    )
//                    signOut()
//                    callback(statusInfo)
//                    return@withTimeout statusInfo
//                }
//
//                val statusInfo = FuncStatusInfoWithCode(
//                    status = FuncStatus.SUCCESS,
//                    errorMessage = ""
//                )
//                callback(statusInfo)
//                statusInfo
//            }
//        } catch (e: TimeoutCancellationException) {
//            val statusInfo = FuncStatusInfoWithCode(
//                status = FuncStatus.TIMEOUT,
//                errorMessage = "タイムアウトしました"
//            )
//            callback(statusInfo)
//            statusInfo
//        } catch (e: FirebaseAuthException) {
//            val statusInfo = FuncStatusInfoWithCode(
//                status = FuncStatus.FAILED,
//                errorMessage = e.message ?: "予期せぬエラーが発生しました",
//                errorCode = e.errorCode
//            )
//            callback(statusInfo)
//            statusInfo
//        } catch (e: Exception) {
//            val statusInfo = FuncStatusInfoWithCode(
//                status = FuncStatus.FAILED,
//                errorMessage = e.message ?: "予期せぬエラーが発生しました"
//            )
//            callback(statusInfo)
//            statusInfo
//        }
//    }
//
//    suspend fun signUp(
//        email: String,
//        password: String,
//        isSendEmailVerification: Boolean = true,
//        callback: (FuncStatusInfoWithCode) -> Unit
//    ): FuncStatusInfoWithCode {
//        return try {
//            withTimeout(10000) {
//                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
//                if (firebaseAuth.currentUser == null ||
//                    firebaseAuth.currentUser?.uid.isNullOrEmpty()
//                ) {
//                    /* まず起こり得ないが、uidが入っているかチェック */
//                    val statusInfo = FuncStatusInfoWithCode(
//                        status = FuncStatus.FAILED,
//                        errorMessage = "ユーザーIDが取得できませんでした"
//                    )
//                    signOut()//サインアウトしなくてもメイン画面にはいけないと思うが。念の為
//                    callback(statusInfo)
//                    return@withTimeout statusInfo
//                }
//
//                if (isSendEmailVerification) {
//                    firebaseAuth.currentUser?.sendEmailVerification()
//                }
//
//                val statusInfo = FuncStatusInfoWithCode(
//                    status = FuncStatus.SUCCESS,
//                    errorMessage = ""
//                )
//                callback(statusInfo)
//                statusInfo
//            }
//        } catch (e: TimeoutCancellationException) {
//            val statusInfo = FuncStatusInfoWithCode(
//                status = FuncStatus.TIMEOUT,
//                errorMessage = "タイムアウトしました"
//            )
//            callback(statusInfo)
//            statusInfo
//        } catch (e: FirebaseAuthException) {
//            val statusInfo = FuncStatusInfoWithCode(
//                status = FuncStatus.FAILED,
//                errorMessage = e.message ?: "予期せぬエラーが発生しました",
//                errorCode = e.errorCode
//            )
//            callback(statusInfo)
//            statusInfo
//        } catch (e: Exception) {
//            val statusInfo = FuncStatusInfoWithCode(
//                status = FuncStatus.FAILED,
//                errorMessage = e.message ?: "予期せぬエラーが発生しました"
//            )
//            callback(statusInfo)
//            statusInfo
//        }
//    }
//
//    fun signOut(): SignOutResult {
//        try {
//            expenseSharedViewModel.onSignedOut()
//
//            firebaseAuth.signOut()
//            if (userId == null) {
//                Log.d("AuthManagerViewModel", "signOut successful")
//                _currentUser.value = null
//                return SignOutResult.SUCCESS
//            } else {
//                //サイン・アウトしてからすぐだとここに来てしまうが、時間経って結局nullになる。
//                //したがって、扱いとしてはSUCCESSにする
//                Log.d(
//                    "AuthManagerViewModel",
//                    "!!Warning!!signOut failed. currentUser is not null\n"
//                )
//                _currentUser.value = null
//                return SignOutResult.SUCCESS
//            }
//        } catch (e: Exception) {
//            Log.d("AuthManagerViewModel", "signOut failed : ${e.message}")
//            return SignOutResult.SIGN_OUT_FAILED
//        }
//    }
//
//    /**
//     * シンプルなsignIn関数が定義してあって、
//     * こっちが実際にViewで呼び出す方。
//     */
//    fun signInWithCallback(
//        email: String,
//        password: String,
//        isEmailVerification: Boolean = true,
//        callback: (FuncStatusInfoWithCode) -> Unit
//    ) {
//        viewModelScope.launch {
//            signIn(email, password, isEmailVerification, callback = callback)
//        }
//    }
//
//    fun signUpWithCallback(
//        email: String,
//        password: String,
//        isSendEmailVerification: Boolean = true,
//        callback: (FuncStatusInfoWithCode) -> Unit
//    ) {
//        viewModelScope.launch {
//            val signUpStatus =
//                signUp(
//                    email,
//                    password,
//                    isSendEmailVerification = isSendEmailVerification,
//                    callback = {})
//            if (signUpStatus.status != FuncStatus.SUCCESS) {
//                callback(signUpStatus)
//                return@launch
//            }
//
//            //成功したときのみ
//            expenseSharedViewModel.addInitialCategories(callback = {})
//            callback(signUpStatus)
//        }
//    }
//
//    /**
//     * @FIXME isSignedInと統一したほうがいいかもな？
//     */
//    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser)
//    val currentUser: StateFlow<FirebaseUser?> = _currentUser
//    private val _signInLoading = MutableStateFlow(false)
//    val signInLoading: StateFlow<Boolean> = _signInLoading
//
//    suspend fun signInWithGoogleIdToken(idToken: String) {
//        _signInLoading.value = true
//
//        try {
//            val credential = GoogleAuthProvider.getCredential(idToken, null)
//            val result = withContext(Dispatchers.IO) {
//                firebaseAuth.signInWithCredential(credential).await()
//            }
//            _currentUser.value = result.user
//            // 新規ユーザーなら初期カテゴリを追加
//            if (result.additionalUserInfo?.isNewUser == true) {
//                expenseSharedViewModel.addInitialCategories(callback = {})
//            }
////            _signInLoading.value = false
//        } catch (e: Exception) {
//            Log.e("AuthViewModel", "Sign-in failed", e)
//            _currentUser.value = null
//            _signInLoading.value = false
//        }
//    }
}