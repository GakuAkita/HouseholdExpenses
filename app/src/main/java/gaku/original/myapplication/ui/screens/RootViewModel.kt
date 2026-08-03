package gaku.original.myapplication.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.auth.AuthRepository
import timber.log.Timber

class RootViewModel(
    private val authRepository: AuthRepository
): ViewModel() {

    private val _authState = authRepository.authState
    val authState get()=_authState

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val authRepository = app.appContainer.authRepository
                RootViewModel(authRepository)
            }
        }
    }

    init{
        Timber.d("Created: ${hashCode()}")
    }

    override fun onCleared() {
        Timber.d("onCleared() called. ${hashCode()}")
        super.onCleared()
    }
}