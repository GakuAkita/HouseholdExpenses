package gaku.original.myapplication.viewModel

import androidx.lifecycle.ViewModel
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.fromLocalDateTime
import gaku.original.myapplication.toLocalDateTime
import java.time.LocalDate
import java.time.LocalTime

class ExpenseAddEditViewModel(
    private val tmpExpenseViewModel: TemporaryExpenseViewModel,
    private val expenseSharedViewModel: ExpenseSharedViewModel
) : ViewModel() {

    // プロパティでアクセスを簡略化。この書き方でcurrentTmpExpenseだけでviewModelのtmpExpenseにアクセスできる。
    private var currentTmpExpense: Expense
        get() = tmpExpenseViewModel.tmpExpense.value
        set(value) {
            tmpExpenseViewModel.tmpExpense.value = value
        }

    fun getCurrentTmpExpense():Expense{
        return currentTmpExpense
    }

    // 日付のみを更新する
    fun updateTmpExpenseDate(newDate: LocalDate) {
        currentTmpExpense = currentTmpExpense.copy(
            datetime = fromLocalDateTime(
                toLocalDateTime(currentTmpExpense.datetime)
                    ?.withYear(newDate.year)
                    ?.withMonth(newDate.monthValue)
                    ?.withDayOfMonth(newDate.dayOfMonth)
            )
        )
    }

    // 時間のみを更新する
    fun updateTmpExpenseTime(newTime: LocalTime) {
        currentTmpExpense = currentTmpExpense.copy(
            datetime = fromLocalDateTime(
                toLocalDateTime(currentTmpExpense.datetime)
                    ?.withHour(newTime.hour)
                    ?.withMinute(newTime.minute)
                    ?.withSecond(newTime.second)
            )
        )
    }

    // 各項目を個別に更新するメソッド
    fun updateTmpExpenseAmount(newAmount: Long?) {
        currentTmpExpense = currentTmpExpense.copy(amount = newAmount)
    }

    fun updateTmpExpenseCategory(newCategory: String) {
        currentTmpExpense = currentTmpExpense.copy(category = newCategory)
    }

    fun updateTmpExpenseNote(newNote: String) {
        currentTmpExpense = currentTmpExpense.copy(note = newNote)
    }

    fun addTmpExpenseToDb() {
        expenseSharedViewModel.addExpense(currentTmpExpense)
    }

    fun updateTmpExpenseToDb() {
        expenseSharedViewModel.updateExpense(currentTmpExpense)
    }

    fun removeTmpExpenseToDb() {
        expenseSharedViewModel.removeExpense(currentTmpExpense)
    }

    fun resetTmpExpense(){
        tmpExpenseViewModel.resetTmpExpense()
    }
}
