package gaku.original.myapplication.viewModel

import androidx.lifecycle.ViewModel
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.fromLocalDateTime
import gaku.original.myapplication.toLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ExpenseAddEditViewModel(
    private val tmpExpenseViewModel: TemporaryExpenseViewModel,
    private val expenseSharedViewModel: ExpenseSharedViewModel
) : ViewModel() {

    // プロパティでアクセスを簡略化。この書き方でcurrentExpenseだけでviewModelのtmpExpenseにアクセスできる。
    private var currentExpense: Expense
        get() = tmpExpenseViewModel.tmpExpense.value
        set(value) {
            tmpExpenseViewModel.tmpExpense.value = value
        }

    // 日付のみを更新する
    fun updateTmpExpenseDate(newDate: LocalDate) {
        currentExpense = currentExpense.copy(
            datetime = fromLocalDateTime(
                toLocalDateTime(currentExpense.datetime)
                    ?.withYear(newDate.year)
                    ?.withMonth(newDate.monthValue)
                    ?.withDayOfMonth(newDate.dayOfMonth)
            )
        )
    }

    // 時間のみを更新する
    fun updateTmpExpenseTime(newTime: LocalTime) {
        currentExpense = currentExpense.copy(
            datetime = fromLocalDateTime(
                toLocalDateTime(currentExpense.datetime)
                    ?.withHour(newTime.hour)
                    ?.withMinute(newTime.minute)
                    ?.withSecond(newTime.second)
            )
        )
    }

    // 各項目を個別に更新するメソッド
    fun updateTmpExpenseAmount(newAmount: Long?) {
        currentExpense = currentExpense.copy(amount = newAmount)
    }

    fun updateTmpExpenseCategory(newCategory: String) {
        currentExpense = currentExpense.copy(category = newCategory)
    }

    fun updateTmpExpenseNote(newNote: String) {
        currentExpense = currentExpense.copy(note = newNote)
    }

    // ExpenseInstanceを一旦リセットする
    fun resetTmpExpense() {
        currentExpense = Expense(
            id = null,
            datetime = fromLocalDateTime(LocalDateTime.now()),
            amount = null,
            category = null,
            note = null,
            generatedType = null
        )
    }

    fun addTmpExpenseToDb() {
        expenseSharedViewModel.addExpense(currentExpense)
    }

    fun updateTmpExpenseToDb() {
        expenseSharedViewModel.updateExpense(currentExpense)
    }

    fun removeTmpExpenseToDb() {
        expenseSharedViewModel.removeExpense(currentExpense)
    }
}
