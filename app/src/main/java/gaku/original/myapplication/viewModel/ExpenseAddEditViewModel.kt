package gaku.original.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.Utility.fromLocalDateTime
import gaku.original.myapplication.Utility.toInstantUTC
import gaku.original.myapplication.Utility.toLocalDateTime
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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
    fun getTimeZoneDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
        /* とりあえず日本で固定 */
        toInstantUTC(currentTmpExpense.datetime)
        val _zoneId: ZoneId = ZoneId.of("Asia/Tokyo")
        return LocalDate.now(_zoneId)
    }

    /* 設定のタイムゾーンに合わせた現在時間 */
    fun getTimeZoneTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalTime {
        /* とりあえず日本で固定 */
        val _zoneId: ZoneId = ZoneId.of("Asia/Tokyo")
        return LocalTime.now(_zoneId)
    }

    // 日付のみを更新する
    fun updateTmpExpenseDate(newDate: LocalDate) {
        tmpExpenseViewModel.updateTmpExpense(
            currentTmpExpense.copy(
                datetime = fromLocalDateTime(
                    toLocalDateTime(currentTmpExpense.datetime)
                        ?.withYear(newDate.year)
                        ?.withMonth(newDate.monthValue)
                        ?.withDayOfMonth(newDate.dayOfMonth)
                )
            )
        )
    }

    // 時間のみを更新する
    fun updateTmpExpenseTime(newTime: LocalTime) {
        tmpExpenseViewModel.updateTmpExpense(
            currentTmpExpense.copy(
                datetime = fromLocalDateTime(
                    toLocalDateTime(currentTmpExpense.datetime)
                        ?.withHour(newTime.hour)
                        ?.withMinute(newTime.minute)
                        ?.withSecond(newTime.second)
                )
            )
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
            expenseSharedViewModel.addExpense(currentTmpExpense, callback)
        }
    }

    fun updateTmpExpenseToDb(onStart: () -> Unit, callback: (SuspendFuncStatusInfo) -> Unit) {
        onStart()
        viewModelScope.launch {
            expenseSharedViewModel.updateExpense(currentTmpExpense, callback)
        }
    }

    fun removeTmpExpenseToDb(onStart: () -> Unit, callback: (SuspendFuncStatusInfo) -> Unit) {
        onStart()
        viewModelScope.launch {
            expenseSharedViewModel.removeExpense(currentTmpExpense, callback)
        }
    }

    /* カテゴリーを更新する。通信エラーが起きているとカテゴリーが取れていないときがある */
    fun updateStoredCategories(
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        expenseSharedViewModel.clearAllCategories()
        viewModelScope.launch {
            expenseSharedViewModel.fetchAllCategories(
                callback = {
                    expenseSharedViewModel.addCategoryListeners()
                    callback(it)
                }
            )
        }
    }
}
