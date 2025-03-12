package gaku.original.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.fromLocalDateTime
import gaku.original.myapplication.toLocalDateTime
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
    val categories: List<Category>
        get() = expenseSharedViewModel.allCategories.value

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

    fun addTmpExpenseToDb(onStart: () -> Unit, callback: (SuspendFuncStatus) -> Unit) {
        onStart()
        viewModelScope.launch {
            expenseSharedViewModel.addExpense(currentTmpExpense, callback)
        }
    }

    fun updateTmpExpenseToDb(onStart: () -> Unit, callback: (SuspendFuncStatus) -> Unit) {
        onStart()
        viewModelScope.launch {
            expenseSharedViewModel.updateExpense(currentTmpExpense, callback)
        }
    }

    fun removeTmpExpenseToDb(onStart: () -> Unit, callback: (SuspendFuncStatus) -> Unit) {
        onStart()
        viewModelScope.launch {
            expenseSharedViewModel.removeExpense(currentTmpExpense, callback)
        }
    }
}
