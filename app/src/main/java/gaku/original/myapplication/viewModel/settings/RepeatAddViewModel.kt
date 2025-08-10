package gaku.original.myapplication.viewModel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Constants.getDaysInMonthByFrequency
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.useCase.RepeatAddUseCase
import gaku.original.myapplication.utility.AppTimeZone
import gaku.original.myapplication.viewModel.ExpenseSharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepeatAddViewModel @Inject constructor(
    private val expenseSharedViewModel: ExpenseSharedViewModel,
    private val repeatAddUseCase: RepeatAddUseCase
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
            val fetchResult = repeatAddUseCase.fetchAllRepeatAdd()
            if (fetchResult is FuncResultWithData.Success) {
                _repeatAddSettings.value = fetchResult.data
            } else {
                _repeatAddSettings.value = emptyList()
            }
            callback(fetchResult.toSuspendFuncStatusInfo())
        }
    }

    suspend fun addRepeatAdd(repeatAdd: RepeatAdd): FuncResultWithData<RepeatAdd> {
        return repeatAddUseCase.addRepeatAdd(repeatAdd)
    }

    fun addRepeatAddSetting(repeatAdd: RepeatAdd, callback: (SuspendFuncStatusInfo) -> Unit = {}) {
        //チェックをいれる
        viewModelScope.launch {
            val ret = addRepeatAdd(repeatAdd)
            callback(ret.toSuspendFuncStatusInfo())
        }
    }

    fun updateRepeatAdd(repeatAdd: RepeatAdd, callback: (SuspendFuncStatusInfo) -> Unit = {}) {
        viewModelScope.launch {
            val ret = repeatAddUseCase.updateRepeatAdd(repeatAdd)
            callback(ret)
        }
    }

    fun removeRepeatAdd(repeatAdd: RepeatAdd, callback: (SuspendFuncStatusInfo) -> Unit = {}) {
        viewModelScope.launch {
            val ret = repeatAddUseCase.removeRepeatAdd(repeatAdd)
            callback(ret)
        }
    }

    /**
     * RepeatAddをしたあと、月末まで追加する
     */
    fun addExpensesForRestOfDays(
        repeatAdd: RepeatAdd,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ) {
        /* RepeatAddのexpenseのgeneratedTypeは入っていないのでここで入れないとだめ */
        viewModelScope.launch {
            val ret = addRepeatAdd(repeatAdd)
            if (ret !is FuncResultWithData.Success) {
                callback(ret.toSuspendFuncStatusInfo())
                return@launch
            }

            /* データからidを取り出す。generatedTypeに使うため */
            val id = repeatAdd.id
            if (id == null) {
                callback(
                    SuspendFuncStatusInfo(
                        status = SuspendFuncStatus.FAILED,
                        errorMessage = "繰り返し追加設定を追加できましたが、idの取得に失敗しました"
                    )
                )
                return@launch
            }

            /**
             * frequencyのデータから今月分の日付全部抽出して、
             * その後、今日以降のものをフィルターすればいいか
             */
            val daysList = getDaysInMonthByFrequency(repeatAdd.frequencyInfo)
            /* 今日の日時の翌日でフィルターを掛けたい */
            val today = AppTimeZone.getCurrentTimeInZone()
            val tomorrowMidnight = today.toLocalDate().plusDays(1).atStartOfDay()

            val addDays = daysList.filter { it >= tomorrowMidnight }

            for (day in addDays){
                val stat=
            }
        }
    }
}