package gaku.original.myapplication

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import gaku.original.myapplication.data.DummyExpenses
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.interfaces.ExpenseDBControl
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/*
rememberの役割
状態の保存：composable関数がrecomposeされる際に状態を維持する。
これにより、ユーザーの操作や外部からのデータ更新によってUIが再描画されたときに、ユーザーが入力した値や選択したオプションがリセットされることがない

mutableStateOfの役割
リアクティブな状態の生成：mutableStateOfは値が変更されると自動的にその値を参照しているUIを再描画する

rememberとViewModelは違う。
 */

class ExpenseViewModel:ViewModel(), ExpenseDBControl {
    /********************* MainView用*******************************/
    private val calendarDate = LocalDate.now()//こいつはmutableStateである必要はない
    private val monthOffset = mutableIntStateOf(0)

    fun getCalendarYear(): Int {
        return calendarDate.plusMonths(monthOffset.intValue.toLong()).year
    }

    fun getCalendarMonth():Int{
        return calendarDate.plusMonths(monthOffset.intValue.toLong()).monthValue
    }

    fun resetMonthOffset(){
        monthOffset.intValue=0
    }

    fun updateMonthOffset(offset:Int){
        monthOffset.intValue=offset
    }

    fun incrementMonth(){
        monthOffset.intValue++
    }

    fun decrementMonth(){
        monthOffset.intValue--
    }

    //MainViewもLazyColumnに表示する
    fun getMonthExpenses():List<Expense>{
        val calendarYear=getCalendarYear()
        val calendarMonth=getCalendarMonth()

        Log.d("<Akita Debug>Recomp Check","executed getMonthExpenses")

        return DummyExpenses.expensesList.filter {
            it.datetime.year == calendarYear &&
                    it.datetime.monthValue == calendarMonth
        }
    }

    /********************* AddEditView用*******************************/
    // 初期値として null もしくは適切なデフォルト値を設定
    private val _expense = mutableStateOf<Expense?>(null)
    val expense: State<Expense?> = _expense

    // Expense のインスタンスを更新するメソッド
    fun updateExpenseInstance(newExpense: Expense) {
        _expense.value = newExpense
    }

    // 日付のみを更新する
    fun updateExpenseInstanceDate(newDate: LocalDate) {
        _expense.value?.let {
            _expense.value = it.copy(
                datetime = it.datetime
                    .withYear(newDate.year)
                    .withMonth(newDate.monthValue)
                    .withDayOfMonth(newDate.dayOfMonth)
            )
        }
    }

    //時間のみを更新する
    fun updateExpenseInstanceTime(newTime: LocalTime) {
        _expense.value?.let { currentExpense ->
            _expense.value = currentExpense.copy(
                datetime = currentExpense.datetime
                    .withHour(newTime.hour)
                    .withMinute(newTime.minute)
                    .withSecond(newTime.second)
            )
        }
    }

    // 各項目を個別に更新するメソッド
    fun updateExpenseInstanceAmount(newAmount: Long) {
        _expense.value?.let {
            _expense.value = it.copy(amount = newAmount)
        }
    }

    fun updateExpenseInstanceCategory(newCategory: String) {
        _expense.value?.let {
            _expense.value = it.copy(category = newCategory)
        }
    }

    fun updateExpenseInstanceNote(newNote: String) {
        _expense.value?.let {
            _expense.value = it.copy(note = newNote)
        }
    }

    //idがDB内にあるかどうかで追加か更新かが決まる
}