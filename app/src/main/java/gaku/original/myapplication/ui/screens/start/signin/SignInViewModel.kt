package gaku.original.myapplication.ui.screens.start.signin

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.data.repository.auth.GoogleSignIn
import gaku.original.myapplication.data.repository.auth.SignInRequest
import gaku.original.myapplication.data.repository.auth.SignUpRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

// https://developer.android.com/topic/architecture/views/ui-layer/events-views?utm_source=chatgpt.com#handle-viewmodel-events
data class SignInUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isGoogleEnabled: Boolean = false,
    val email: String = "",
    val password: String = "",
)

//sealed interface SignInMethod {
//    data class Email(
//        val email: String,
//        val password: String
//    ) : SignInMethod
//
//    data object Google: SignInMethod
//}

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

    init {
        Timber.d("Created!!!!${hashCode()}")

        if (authRepository is GoogleSignIn) {
            _uiState.update {
                it.copy(
                    isGoogleEnabled = true
                )
            }
        }
    }

    fun onMessageShown() {
        _uiState.update {
            it.copy(message = null)
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(email = value)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(password = value)
        }
    }

    fun signInWithEmail() {
        viewModelScope.launch {
            try {
                val request = SignInRequest.Email(
                    email = _uiState.value.email,
                    password = _uiState.value.password
                )
                _uiState.update {
                    it.copy(isLoading = true)
                }
                authRepository.signIn(request)
                _uiState.update {
                    it.copy(
                        message = "Sign in Successful"
                    )
                }
                /* navigation is done at the root */
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = e.message
                    )
                }
            }
        }
    }

    suspend fun signInWithGoogle(activity: Activity) {
        try {
            _uiState.update {
                it.copy(
                    isLoading = true
                )
            }

            if (authRepository is GoogleSignIn) {
                authRepository.signInWithGoogle(activity)
            } else {
                throw Exception("Bug: authRepository is not GoogleSignIn")
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = e.message
                )
            }
        }
    }

    fun signUpWithEmail() {
        Timber.d("SignUpWithEmail Called.")
        viewModelScope.launch {
            try {
                val request = SignUpRequest.Email(
                    email = _uiState.value.email,
                    password = _uiState.value.password
                )
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }
                authRepository.signUp(request)
                _uiState.update {
                    it.copy(
                        message = "Sign up Successful."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = e.message
                    )
                }
            }
        }
    }
}