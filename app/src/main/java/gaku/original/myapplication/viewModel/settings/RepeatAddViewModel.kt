package gaku.original.myapplication.viewModel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.Repository.FirestoreRepository.RepeatAddFirestoreRepository
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.viewModel.ExpenseSharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepeatAddViewModel @Inject constructor(
    private val expenseSharedViewModel: ExpenseSharedViewModel,
    private val repeatAddRepository: RepeatAddFirestoreRepository
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
    fun fetchAllRepeatAddSettings(callback: (SuspendFuncStatusInfo) -> Unit = {}) {
        viewModelScope.launch {
            val fetchResult = repeatAddRepository.fetchAllRepeatAdd()
            if (fetchResult is FetchResult.Success) {
                _repeatAddSettings.value = fetchResult.data
            } else {
                _repeatAddSettings.value = emptyList()
            }
            callback(fetchResult.toSuspendFuncStatusInfo())
        }
    }

    fun addRepeatAddSetting(repeatAdd: RepeatAdd, callback: (SuspendFuncStatusInfo) -> Unit = {}) {

        //チェックをいれる
        viewModelScope.launch {
            val ret = repeatAddRepository.addRepeatAdd(repeatAdd)
            callback(ret)
        }
    }

    fun updateRepeatAdd(repeatAdd: RepeatAdd, callback: (SuspendFuncStatusInfo) -> Unit = {}) {
        viewModelScope.launch {
            val ret = repeatAddRepository.updateRepeatAdd(repeatAdd)
            callback(ret)
        }
    }

    fun removeRepeatAdd(repeatAdd: RepeatAdd, callback: (SuspendFuncStatusInfo) -> Unit = {}) {
        viewModelScope.launch {
            val ret = repeatAddRepository.removeRepeatAdd(repeatAdd)
            callback(ret)
        }
    }
}