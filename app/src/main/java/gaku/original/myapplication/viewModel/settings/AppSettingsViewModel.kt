package gaku.original.myapplication.viewModel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.repository.FirestoreRepository.UserSettingsFirestoreRepository
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
        callback: (FuncStatusInfo) -> Unit
    ) {
        viewModelScope.launch {
            val ret = userPreferencesRepository.setUserTimeZone(timeZone)
            callback(ret)
        }
    }
}