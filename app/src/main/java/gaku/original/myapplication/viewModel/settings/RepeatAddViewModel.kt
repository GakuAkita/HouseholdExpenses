package gaku.original.myapplication.viewModel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.useCase.RepeatAddUseCase
import gaku.original.myapplication.viewModel.shared.ExpenseSharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepeatAddViewModel @Inject constructor(
    private val expenseSharedViewModel: ExpenseSharedViewModel,
    private val repeatAddUseCase: RepeatAddUseCase
) : ViewModel() {

    val allCategories: StateFlow<List<Category>> get() = expenseSharedViewModel.allCategories

    private val _repeatAddSettings = MutableStateFlow<List<RepeatAdd>>(emptyList())
    val repeatAddSettings: StateFlow<List<RepeatAdd>> get() = _repeatAddSettings

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    //ページを開くたびロードする感じで良い。頻度はそんなに多くないから
    fun fetchAllRepeatAddSettings(callback: (FuncStatusInfo) -> Unit = {}) {
        viewModelScope.launch {
//            val fetchResult = repeatAddUseCase.fetchAllRepeatAdd()
//            if (fetchResult is FuncResultWithData.Success) {
//                _repeatAddSettings.value = fetchResult.data
//            } else {
//                _repeatAddSettings.value = emptyList()
//            }
//            callback(fetchResult.toFuncStatusInfo())
        }
    }

//    suspend fun addRepeatAdd(repeatAdd: RepeatAdd): FuncResultWithData<RepeatAdd> {
//        return repeatAddUseCase.addRepeatAdd(repeatAdd)
//    }

    fun addRepeatAddSetting(
        repeatAdd: RepeatAdd,
        callback: (FuncResultWithData<RepeatAdd>) -> Unit = {}
    ) {
        //チェックをいれる
        viewModelScope.launch {
//            val ret = addRepeatAdd(repeatAdd)
//            callback(ret)
        }
    }

    fun updateRepeatAdd(repeatAdd: RepeatAdd, callback: (FuncStatusInfo) -> Unit = {}) {
        viewModelScope.launch {
//            val ret = repeatAddUseCase.updateRepeatAdd(repeatAdd)
//            callback(ret)
        }
    }

    fun removeRepeatAdd(repeatAdd: RepeatAdd, callback: (FuncStatusInfo) -> Unit = {}) {
        viewModelScope.launch {
//            val ret = repeatAddUseCase.removeRepeatAdd(repeatAdd)
//            callback(ret)
        }
    }

    fun initProgress() {
        _progress.value = 0f
    }

    /**
     * RepeatAddをしたあと、月末まで追加する
     */
    fun addExpensesForRestOfDays(
        repeatAdd: RepeatAdd,
        callback: (FuncStatusInfo) -> Unit = {}
    ) {
        /* RepeatAddのexpenseのgeneratedTypeは入っていないのでここで入れないとだめ */
        viewModelScope.launch {
            repeatAddUseCase.addExpensesForRestOfDaysFlow(repeatAdd).collect { (progress, status) ->
                _progress.value = progress
                if (status != null) {
                    callback(status)
                }
            }
        }
    }
}