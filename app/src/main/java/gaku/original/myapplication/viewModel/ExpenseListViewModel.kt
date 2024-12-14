package gaku.original.myapplication.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.ExpenseRepository
import gaku.original.myapplication.fromLocalDateTime
import gaku.original.myapplication.toLocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

class ExpenseListViewModel(
    private val expenseSharedViewModel: ExpenseSharedViewModel
):ViewModel(){
    /********************* MainView用*******************************/
    private val calendarDate = LocalDate.now()//こいつはmutableStateである必要はない

    private val _monthOffset = MutableStateFlow(0) // MutableStateFlowに変更
    val monthOffset: StateFlow<Int> = _monthOffset

    fun getCalendarYear(): Int {
        return calendarDate.plusMonths(monthOffset.value.toLong()).year
    }

    fun getCalendarMonth():Int{
        return calendarDate.plusMonths(monthOffset.value.toLong()).monthValue
    }

    fun resetMonthOffset(){
        _monthOffset.value=0
    }

    fun updateMonthOffset(offset:Int){
        _monthOffset.value=offset
    }

    fun incrementMonth(){
        _monthOffset.value++
    }

    fun decrementMonth(){
        _monthOffset.value--
    }

    /********************Repositoryを使う*****************************/
    private val _allExpenses:List<Expense> get() = expenseSharedViewModel.allExpense.value

    private val _filteredExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val filteredExpenses: StateFlow<List<Expense>> get() = _filteredExpenses

    fun filterExpensesByMonth() {
        val targetYear = getCalendarYear()
        val targetMonth = getCalendarMonth()

        _filteredExpenses.value = expenseSharedViewModel.allExpense.value
            .filter { expense ->
                val expenseYear = toLocalDateTime(expense.datetime)?.year ?: 0
                val expenseMonth = toLocalDateTime(expense.datetime)?.monthValue ?: 0
                expenseYear == targetYear && expenseMonth == targetMonth
            }
            .sortedByDescending {
                it.datetime
            }
//        Log.d("ExpenseViewModel","filterExpensesByMonth was executed.↓")
//        Log.d("ExpenseViewModel","${_filteredExpenses.value}")
    }

    /*
    A:AllExpensesを取って、それを月ごとに抽出
    B:前後12ヶ月分だけローカルに保存して、ローカルと変化があったときに同期させる
    C:
    */



    /********************* AddEditView用*******************************/
    // 初期値として null もしくは適切なデフォルト値を設定
    private val _expense = mutableStateOf(
        Expense(
            id = null,
            datetime = fromLocalDateTime(LocalDateTime.now()),
            amount = null,
            category = null,
            note = null,
            generatedType = null
        )
    )
    val expense: State<Expense> = _expense



    // 日付のみを更新する
    fun updateExpenseInstanceDate(newDate: LocalDate) {
        _expense.value.let {currentExpense ->
            _expense.value = currentExpense.copy(
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
    fun updateExpenseInstanceTime(newTime: LocalTime) {
        _expense.value.let { currentExpense ->
            _expense.value = currentExpense.copy(
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
    fun updateExpenseInstanceAmount(newAmount: Long?) {
        _expense.value.let {
            _expense.value = it.copy(amount = newAmount)
        }
    }

    fun updateExpenseInstanceCategory(newCategory: String) {
        _expense.value.let {
            _expense.value = it.copy(category = newCategory)
        }
    }

    fun updateExpenseInstanceNote(newNote: String) {
        _expense.value.let {
            _expense.value = it.copy(note = newNote)
        }
    }

    //ExpenseInstanceを一旦リセットする
    fun resetExpenseInstance(){
        val emptyExpense=Expense(
            id=null,
            datetime= fromLocalDateTime(LocalDateTime.now()),
            amount=null,
            category=null,
            note=null,
            generatedType = null
        )
        updateExpenseInstance(emptyExpense)
    }
}