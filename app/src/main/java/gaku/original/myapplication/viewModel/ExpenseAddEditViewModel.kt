package gaku.original.myapplication.viewModel

import androidx.lifecycle.ViewModel
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.fromLocalDateTime
import gaku.original.myapplication.toLocalDateTime
import java.time.LocalDate
import java.time.LocalTime

class ExpenseAddEditViewModel(
    private val expenseSharedViewModel: ExpenseSharedViewModel,
    private val tmpExpenseViewModel: TemporaryExpenseViewModel
) : ViewModel() {

    // プロパティでアクセスを簡略化。この書き方でcurrentTmpExpenseだけでviewModelのtmpExpenseにアクセスできる。
    val currentTmpExpense: Expense
        get() = tmpExpenseViewModel.tmpExpense.value

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

    fun updateTmpExpenseCategory(newCategory: String) {
        tmpExpenseViewModel.updateTmpExpense(
            currentTmpExpense.copy(category = newCategory)
        )
    }

    fun updateTmpExpenseNote(newNote: String) {
        tmpExpenseViewModel.updateTmpExpense(
            currentTmpExpense.copy(note = newNote)
        )
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
