package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

data class RepeatAddUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val showAddEditDialog: Boolean = false,
    val showAddExpenseConfirmDialog:Boolean = false
)

class RepeatAddViewModel(

) : ViewModel() {
    private val _uiState = MutableStateFlow(RepeatAddUiState())
    val uiState: StateFlow<RepeatAddUiState> = _uiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RepeatAddViewModel()
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}