package gaku.original.myapplication.viewModel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.utility.AppTimeZone
import gaku.original.myapplication.viewModel.ExpenseSharedViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class ExpenseAddEditViewModel @Inject constructor(
    private val expenseSharedViewModel: ExpenseSharedViewModel,
    private val tmpExpenseViewModel: TemporaryExpenseViewModel
) : ViewModel() {

    // プロパティでアクセスを簡略化。この書き方でcurrentTmpExpenseだけでviewModelのtmpExpenseにアクセスできる。
    val currentTmpExpense: Expense
        get() = tmpExpenseViewModel.tmpExpense.value

    //これリアルタイム同期するのか？ 他端末からCategoryを追加してみて、反映されるかみてみる
    val allCategories: StateFlow<List<Category>> get() = expenseSharedViewModel.allCategories

    /* 設定のタイムゾーンに合わせて現在日付 */
    fun getTimeZoneDate(): LocalDate {
        /* とりあえず日本で固定 */
        AppTimeZone.isoStringToLocalDateTime(currentTmpExpense.datetime)?.let {
            return it.toLocalDate()
        }
        return AppTimeZone.getCurrentTimeInZone().toLocalDate()
    }

    /* 設定のタイムゾーンに合わせた現在時間 */
    fun getTimeZoneTime(): LocalTime {
        AppTimeZone.isoStringToLocalDateTime(currentTmpExpense.datetime)?.let {
            return it.toLocalTime()
        }
        return AppTimeZone.getCurrentTimeInZone().toLocalTime()
    }

    fun updateTmpExpenseDatetime(datetimeStr: String) {
        tmpExpenseViewModel.updateTmpExpense(
            currentTmpExpense.copy(datetime = datetimeStr)
        )
    }

    // 各項目を個別に更新するメソッド
    fun updateTmpExpenseAmount(newAmount: Long?) {
        tmpExpenseViewModel.updateTmpExpense(
            currentTmpExpense.copy(amount = newAmount)
        )
    }

    fun updateTmpExpenseCategory(newCategory: Category?) {
        tmpExpenseViewModel.updateTmpExpense(
            currentTmpExpense.copy(category = newCategory)
        )
    }

    fun updateTmpExpenseNote(newNote: String) {
        tmpExpenseViewModel.updateTmpExpense(
            currentTmpExpense.copy(note = newNote)
        )
    }

    fun resetTmpExpense() {
        tmpExpenseViewModel.resetTmpExpense()
    }

    fun addTmpExpenseToDb(onStart: () -> Unit, callback: (SuspendFuncStatusInfo) -> Unit) {
        onStart()
        viewModelScope.launch {
            val ret = expenseSharedViewModel.addExpense(currentTmpExpense)
            callback(ret)
        }
    }

    fun updateTmpExpenseToDb(onStart: () -> Unit, callback: (SuspendFuncStatusInfo) -> Unit) {
        onStart()
        viewModelScope.launch {
            val ret = expenseSharedViewModel.updateExpense(currentTmpExpense)
            callback(ret)
        }
    }

    fun removeTmpExpenseToDb(onStart: () -> Unit, callback: (SuspendFuncStatusInfo) -> Unit) {
        onStart()
        viewModelScope.launch {
            val ret = expenseSharedViewModel.removeExpense(currentTmpExpense)
            callback(ret)
        }
    }

    /* カテゴリーを更新する。通信エラーが起きているとカテゴリーが取れていないときがある */
    fun updateStoredCategories(
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        expenseSharedViewModel.clearAllCategories()
        viewModelScope.launch {
            val ret = expenseSharedViewModel.fetchAllCategories()
            val listenerRet = expenseSharedViewModel.addCategoryListeners()
            callback(ret)
        }
    }
}
