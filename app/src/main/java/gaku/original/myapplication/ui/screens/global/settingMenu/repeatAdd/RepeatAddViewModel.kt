package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.repository.repeatAdd.RepeatAddRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class RepeatAddUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val repeatAdds: List<RepeatAdd> = emptyList()
)

class RepeatAddViewModel(
    private val repeatAddRepository: RepeatAddRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RepeatAddUiState())
    val uiState: StateFlow<RepeatAddUiState> = _uiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY]) as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                RepeatAddViewModel(
                    repeatAddRepository = session.repeatAddRepository
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")
        viewModelScope.launch {
            try{
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }
                val repeatAdds = repeatAddRepository.getAllRepeatAdds()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        repeatAdds = repeatAdds.values.toList()
                    )
                }
            }catch (e:Exception){

            }finally {
                _uiState.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}