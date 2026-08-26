package gaku.original.myapplication.ui.screens.receiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.SharedData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

data class ShareReceiverUiState(
    val sharedData: SharedData? = null
)

class ShareReceiverViewModel(
    private val sharedData: SharedData
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareReceiverUiState())
    val uiState = _uiState.asStateFlow()

    companion object {
        fun Factory(sharedData: SharedData): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ShareReceiverViewModel(
                    sharedData
                )
            }
        }
    }

    init {
        Timber.d("init() called.${hashCode()}")
        _uiState.value = ShareReceiverUiState(
            sharedData
        )
    }

    override fun onCleared() {
        Timber.d("onCleared() called.${hashCode()}")
        super.onCleared()
    }
}