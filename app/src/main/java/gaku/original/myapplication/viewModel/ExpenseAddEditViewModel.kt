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
) : ViewModel(){

    fun getTmpExpense(): Expense {
        return tmpExpenseViewModel.tmpExpense.value
    }

    // 日付のみを更新する
    fun updateTmpExpenseDate(newDate: LocalDate) {
        tmpExpenseViewModel.tmpExpense.value.let {currentExpense ->
             tmpExpenseViewModel.tmpExpense.value = currentExpense.copy(
                //datetimeはstringなので、更新して
                datetime = fromLocalDateTime(
                    toLocalDateTime(currentExpense.datetime)
                        ?.withYear(newDate.year)
                        ?.withMonth(newDate.monthValue)
                        ?.withDayOfMonth(newDate.dayOfMonth)
                )
            )
        }
    }

    //時間のみを更新する
    fun updateTmpExpenseTime(newTime: LocalTime) {
        tmpExpenseViewModel.tmpExpense.value.let { currentExpense ->
            tmpExpenseViewModel.tmpExpense.value = currentExpense.copy(
                datetime = fromLocalDateTime(
                    toLocalDateTime(currentExpense.datetime)
                        ?.withHour(newTime.hour)
                        ?.withMinute(newTime.minute)
                        ?.withSecond(newTime.second)
                )
            )
        }
    }

    // 各項目を個別に更新するメソッド
    fun updateTmpExpenseAmount(newAmount: Long?) {
        tmpExpenseViewModel.tmpExpense.value.let {
            tmpExpenseViewModel.tmpExpense.value = it.copy(amount = newAmount)
        }
    }

    fun updateTmpExpenseCategory(newCategory: String) {
        tmpExpenseViewModel.tmpExpense.value.let {
            tmpExpenseViewModel.tmpExpense.value = it.copy(category = newCategory)
        }
    }

    fun updateTmpExpenseNote(newNote: String) {
        tmpExpenseViewModel.tmpExpense.value.let {
            tmpExpenseViewModel.tmpExpense.value = it.copy(note = newNote)
        }
    }

    //ExpenseInstanceを一旦リセットする
    fun resetTmpExpense(){
        val emptyExpense=Expense(
            id=null,
            datetime= fromLocalDateTime(LocalDateTime.now()),
            amount=null,
            category=null,
            note=null,
            generatedType = null
        )
        tmpExpenseViewModel.tmpExpense.value = emptyExpense
    }

    fun addTmpExpenseToDb(){
        expenseSharedViewModel.addExpense(tmpExpenseViewModel.tmpExpense.value)
    }

    fun updateTmpExpenseToDb(){
        expenseSharedViewModel.updateExpense(tmpExpenseViewModel.tmpExpense.value)
    }

    fun removeTmpExpenseToDb(){
        expenseSharedViewModel.removeExpense(tmpExpenseViewModel.tmpExpense.value)
    }
}