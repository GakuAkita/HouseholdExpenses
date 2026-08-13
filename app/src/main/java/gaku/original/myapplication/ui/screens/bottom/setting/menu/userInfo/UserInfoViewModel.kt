package gaku.original.myapplication.ui.screens.bottom.setting.menu.userInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

data class UserInfoUiState(
    val isLoading: Boolean = false,
    val message:String? = null,
    val email:String = ""
)

class UserInfoViewModel(
    private val authRepository: AuthRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(UserInfoUiState())
    val uiState get() = _uiState

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY]) as MyApplication
                val container = app.appContainer
                UserInfoViewModel(
                    authRepository = container.authRepository
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")

        val email = authRepository.user?.email
        if(email == null){
            _uiState.update {
                it.copy(
                    message = "Coding Error: Email is empty"
                )
            }
        }else{
            _uiState.update {
                it.copy(
                    email = email
                )
            }
        }
    }

    fun onMessageShown(){
        _uiState.value = _uiState.value.copy(
            message = null
        )
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}