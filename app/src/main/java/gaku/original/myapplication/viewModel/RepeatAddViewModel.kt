package gaku.original.myapplication.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.RepeatAdd
import gaku.original.myapplication.data.RepeatAddRepository
import gaku.original.myapplication.data.defaultRepeatAdd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class RepeatAddViewModel @Inject constructor(
    private val repeatAddRepository: RepeatAddRepository
) : ViewModel() {

    private val _tmpRepeatAdd = mutableStateOf<RepeatAdd>(
        defaultRepeatAdd
    )

    // 外部には読み取り専用のインターフェースを公開
    val tmpRepeatAdd: State<RepeatAdd> get() = _tmpRepeatAdd

    fun updateRepeatAddExpense(expense: Expense) {
        _tmpRepeatAdd.value = _tmpRepeatAdd.value.copy(expense = expense)
    }

    fun updateRepeatAddFrequency(frequency: String) {
        _tmpRepeatAdd.value = _tmpRepeatAdd.value.copy(frequency = frequency)
    }

    fun resetTmpExpense() {
        _tmpRepeatAdd.value = defaultRepeatAdd
    }


    private val _repeatAddSettings = MutableStateFlow<List<RepeatAdd>>(emptyList())
    val repeatAddSettings: StateFlow<List<RepeatAdd>> get() = _repeatAddSettings

    //ページを開くたびロードする感じで良い。頻度はそんなに多くないから
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

    fun addRepeatAddSetting(repeatAdd: RepeatAdd, callback: (Boolean) -> Unit = {}) {
        //チェックをいれる
        viewModelScope.launch {
            repeatAddRepository.addRepeatAdd(repeatAdd, callback)
        }
    }

    fun updateRepeatAdd(repeatAdd: RepeatAdd, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repeatAddRepository.updateRepeatAdd(repeatAdd, callback)
        }
    }

    fun removeRepeatAdd(repeatAdd: RepeatAdd, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repeatAddRepository.removeRepeatAdd(repeatAdd, callback)
        }
    }
}