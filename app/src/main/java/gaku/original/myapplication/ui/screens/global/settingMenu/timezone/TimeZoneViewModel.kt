package gaku.original.myapplication.ui.screens.global.settingMenu.timezone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.Constants.TimeZone
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class TimeZoneUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val selectedTimeZone: TimeZone = TimeZone.JAPAN
)

class TimeZoneViewModel(
    private val appTimeZoneRepository: AppTimeZoneRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TimeZoneUiState())
    val uiState get() = _uiState

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY]) as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                TimeZoneViewModel(
                    appTimeZoneRepository = session.appTimeZoneRepository
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")

        val zoneId = appTimeZoneRepository.zoneId.value
        _uiState.update {
            it.copy(
                selectedTimeZone = TimeZone.fromId(zoneId.id) ?: TimeZone.JAPAN
            )
        }
    }

    fun onMessageShown() {
        _uiState.update {
            it.copy(
                message = null
            )
        }
    }

    fun onTimeZoneSelected(timeZone: TimeZone) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }
                appTimeZoneRepository.updateZoneId(timeZone.zoneId)
                _uiState.update {
                    it.copy(
                        selectedTimeZone = timeZone
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = e.message
                    )
                }
            } finally {
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