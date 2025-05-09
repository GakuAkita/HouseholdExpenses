package gaku.original.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.RepeatAdd
import gaku.original.myapplication.data.RepeatAddRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepeatAddViewModel @Inject constructor(
    private val expenseSharedViewModel: ExpenseSharedViewModel,
    private val repeatAddRepository: RepeatAddRepository
) : ViewModel() {

//    private val _tmpRepeatAdd = mutableStateOf<RepeatAdd>(
//        defaultRepeatAdd
//    )
//
//    // 外部には読み取り専用のインターフェースを公開
//    val tmpRepeatAdd: State<RepeatAdd> get() = _tmpRepeatAdd
//
//    fun updateRepeatAddExpense(expense: Expense) {
//        _tmpRepeatAdd.value = _tmpRepeatAdd.value.copy(expense = expense)
//    }
//
//    fun updateRepeatAddFrequency(frequency: String) {
//        _tmpRepeatAdd.value = _tmpRepeatAdd.value.copy(frequency = frequency)
//    }
//
//    fun resetTmpExpense() {
//        _tmpRepeatAdd.value = defaultRepeatAdd
//    }

    val allCategories: StateFlow<List<Category>> get() = expenseSharedViewModel.allCategories

    private val _repeatAddSettings = MutableStateFlow<List<RepeatAdd>>(emptyList())
    val repeatAddSettings: StateFlow<List<RepeatAdd>> get() = _repeatAddSettings

    //ページを開くたびロードする感じで良い。頻度はそんなに多くないから
    fun fetchAllRepeatAddSettings(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val settings = repeatAddRepository.fetchRepeatAddSettings(
                callback = { status ->
                    /* 成功失敗時の通知 */
                }
            )

            if (settings.isNotEmpty()) {
                _repeatAddSettings.value = settings
            }
            onComplete()
        }
    }

    fun addRepeatAddSetting(repeatAdd: RepeatAdd, callback: (SuspendFuncStatus) -> Unit = {}) {
        //チェックをいれる
        viewModelScope.launch {
            repeatAddRepository.addRepeatAdd(repeatAdd, callback)
        }
    }

    fun updateRepeatAdd(repeatAdd: RepeatAdd, callback: (SuspendFuncStatus) -> Unit = {}) {
        viewModelScope.launch {
            repeatAddRepository.updateRepeatAdd(repeatAdd, callback)
        }
    }

    fun removeRepeatAdd(repeatAdd: RepeatAdd, callback: (SuspendFuncStatus) -> Unit = {}) {
        viewModelScope.launch {
            repeatAddRepository.removeRepeatAdd(repeatAdd, callback)
        }
    }
}