package gaku.original.myapplication.viewModel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Repository.FirestoreRepository.UserSettingsFirestoreRepository
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserSettingsFirestoreRepository
) : ViewModel() {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    //設定する
    fun setUserTimeZone(
        timeZone: String,
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        viewModelScope.launch {
            userPreferencesRepository.setUserTimeZone(timeZone, callback = callback)
        }
    }
}