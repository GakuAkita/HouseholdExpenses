package gaku.original.myapplication.ui.screens.bottom.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

data class SettingUiState(
    val isLoading: Boolean = false,
    val message:String? = null,
    val isLoggedOut:Boolean = false
)

class SettingViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState get() = _uiState

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY]) as MyApplication
                val container = app.appContainer
                SettingViewModel(
                    authRepository = container.authRepository
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")
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