package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.editDialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.repeatAdd.RepeatAddRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

data class RepeatAddEditDialogState(
    val isLoading: Boolean = false,
    val message: String? = null
)

class RepeatAddEditViewModel(
    private val repeatAddRepository: RepeatAddRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepeatAddEditDialogState())
    val uiState: StateFlow<RepeatAddEditDialogState> = _uiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                RepeatAddEditViewModel(
                    session.repeatAddRepository
                )
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