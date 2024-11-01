package gaku.original.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import gaku.original.myapplication.data.DummyExpenses
import gaku.original.myapplication.data.Expense
import java.time.LocalDate

/*
rememberの役割
状態の保存：composable関数がrecomposeされる際に状態を維持する。
これにより、ユーザーの操作や外部からのデータ更新によってUIが再描画されたときに、ユーザーが入力した値や選択したオプションがリセットされることがない

mutableStateOfの役割
リアクティブな状態の生成：mutableStateOfは値が変更されると自動的にその値を参照しているUIを再描画する

rememberとViewModelは違う。
 */

class ExpenseViewModel():ViewModel() {
    private val calendarDate = LocalDate.now()//こいつはmutableStateである必要はない
    private val monthOffset = mutableStateOf(0)

    fun getCalendarYear(): Int {
        return calendarDate.plusMonths(monthOffset.value.toLong()).year
    }

    fun getCalendarMonth():Int{
        return calendarDate.plusMonths(monthOffset.value.toLong()).monthValue
    }

    fun resetMonthOffset(){
        monthOffset.value=0
    }

    fun updateMonthOffset(offset:Int){
        monthOffset.value=offset
    }

    fun incrementMonth(){
        monthOffset.value++
    }

    fun decrementMonth(){
        monthOffset.value--
    }

    //MainViewもLazyColumnに表示する
    fun getMonthExpenses():List<Expense>{
        val calendarYear=getCalendarYear()
        val calendarMonth=getCalendarMonth()

        return DummyExpenses.expensesList.filter {
            it.datetime.year == calendarYear &&
                    it.datetime.monthValue == calendarMonth
        }
    }
}