package gaku.original.myapplication.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gaku.original.myapplication.data.RepeatAdd
import gaku.original.myapplication.data.RepeatAddRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

//アクセスするたび
class RepeatAddViewModel @Inject constructor(
    private val repeatAddRepository: RepeatAddRepository
) : ViewModel() {

    private val _repeatAddSettings = MutableStateFlow<List<RepeatAdd>>(emptyList())
    val repeatAddSettings: StateFlow<List<RepeatAdd>> get() = _repeatAddSettings

    fun fetchAllRepeatAddSettings(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val settings = repeatAddRepository.fetchRepeatAddSettings(
                callback = { isSuccess ->
                    if (!isSuccess) {
                        Log.d("RepeatAddViewModel", "fetchRepeatAddSettings failed.")
                        //UIに通知したい。
                    }
                }
            )
            if (settings != null) {
                _repeatAddSettings.value = settings
            }
            onComplete()
        }
    }

    fun addRepeatAddSetting(repeatAdd: RepeatAdd) {
        //チェックをいれる
        viewModelScope.launch {
            //
        }
    }

    fun updateRepeatAdd(repeatAdd: RepeatAdd) {
        viewModelScope.launch {
            //
        }
    }

    fun removeRepeatAdd(repeatAdd: RepeatAdd) {
        viewModelScope.launch {
            //
        }
    }
}