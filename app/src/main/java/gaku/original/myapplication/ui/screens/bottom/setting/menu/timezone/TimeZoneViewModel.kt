package gaku.original.myapplication.ui.screens.bottom.setting.menu.timezone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

data class TimeZoneUiState(
    val isLoading: Boolean = false,
    val message:String? = null
)

class TimeZoneViewModel(
    private val appTimeZoneRepository: AppTimeZoneRepository
): ViewModel() {
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

    init{
        Timber.d("Created. ${hashCode()}")
    }



    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}