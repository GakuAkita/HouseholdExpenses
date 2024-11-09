package gaku.original.myapplication

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import gaku.original.myapplication.data.DummyExpenses
import gaku.original.myapplication.data.ExpenseClass
import gaku.original.myapplication.`interface`.ExpenseDBControl
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
    fun getMonthExpenses():List<ExpenseClass>{
        val calendarYear=getCalendarYear()
        val calendarMonth=getCalendarMonth()

        return DummyExpenses.expensesList.filter {
            it.datetime.year == calendarYear &&
                    it.datetime.monthValue == calendarMonth
        }
    }

    /********************* AddEditView用*******************************/
    //privateにしなくていいか。
    val id = mutableStateOf<String?>(null)
    val datetime = mutableStateOf(LocalDateTime.now())
    val expense = mutableStateOf<Long?>(null)
    val category = mutableStateOf<String?>(null)
    val note = mutableStateOf<String?>(null)
    val generatedType=mutableStateOf<String?>(null)

    //初期化
    fun resetExpenseParams(){
        datetime.value= LocalDateTime.now()
        expense.value=null
        category.value=null
        note.value=null
    }

    //転写する
    fun transferExpenseParams(Expense:ExpenseClass){
        id.value=Expense.id
        datetime.value=Expense.datetime
        expense.value=Expense.expense
        category.value=Expense.category
        note.value=Expense.note
    }

    fun dateUpdate(newDate: LocalDate) {
        datetime.value = datetime.value.withYear(newDate.year).withMonth(newDate.monthValue).withDayOfMonth(newDate.dayOfMonth)
    }

    fun timeUpdate(newTime: LocalTime) {
        datetime.value = datetime.value.withHour(newTime.hour).withMinute(newTime.minute).withSecond(newTime.second)
    }

    //expenseの更新
    fun expenseUpdate(newExpense: String) {
        val numericExpense = newExpense.toLongOrNull()
        //桁数がギリギリのときの対応
        if(numericExpense!=null) {
            expense.value = numericExpense
        }
        else{//nullだったら
            expense.value = null
        }
    }

    //categoryの更新

    //noteの更新
    fun noteUpdate(newNote: String) {
        note.value = newNote
    }

    //idがDB内にあるかどうかで追加か更新かが決まる


}