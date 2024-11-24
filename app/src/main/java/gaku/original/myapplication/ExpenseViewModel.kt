package gaku.original.myapplication

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gaku.original.myapplication.data.DummyExpenses
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository = Graph.expenseRepository
):ViewModel() {
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
    lateinit var getAllExpenses: Flow<List<Expense>>

    init {
        viewModelScope.launch {
            getAllExpenses = expenseRepository.getAllExpenses()
        }
    }

    fun getAExpense(id:String):Flow<Expense>{
        return expenseRepository.getExpenseById(id)
    }

    fun addExpense(newExpense:Expense,num:Int){
        viewModelScope.launch(Dispatchers.IO){
            expenseRepository.addAExpense(newExpense,num)
        }
    }

    fun updateExpense(expense:Expense){
        viewModelScope.launch(Dispatchers.IO){
            expenseRepository.updateAExpense(expense=expense)
        }
    }

    fun deleteExpense(expense:Expense){
        viewModelScope.launch() {
            expenseRepository.deleteAExpense(expense=expense)
        }
    }

    /********************* AddEditView用*******************************/
    // 初期値として null もしくは適切なデフォルト値を設定
    private val _expense = mutableStateOf<Expense>(
        Expense(
            id = "",
            datetime = LocalDateTime.now(),
            amount = null,
            category = null,
            note = null,
            generatedType = null
        )
    )
    val expense: State<Expense> = _expense

    fun getExpenseInstanceId():String{
        return _expense.value.id
    }

    fun getExpenseInstanceAmount():Long?{
        return _expense.value.amount
    }

    fun getExpenseInstanceDateTime():LocalDateTime{
        return _expense.value.datetime
    }

    fun getExpenseInstanceCategory():String?{
        return _expense.value.category
    }

    fun getInstanceNote():String?{
        return _expense.value.note
    }

    // Expense のインスタンスを更新するメソッド
    fun updateExpenseInstance(newExpense: Expense) {
        _expense.value = newExpense
    }

    // 日付のみを更新する
    fun updateExpenseInstanceDate(newDate: LocalDate) {
        _expense.value.let {
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
        _expense.value.let { currentExpense ->
            _expense.value = currentExpense.copy(
                datetime = currentExpense.datetime
                    .withHour(newTime.hour)
                    .withMinute(newTime.minute)
                    .withSecond(newTime.second)
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
            id="",
            datetime=LocalDateTime.now(),
            amount=null,
            category=null,
            note=null,
            generatedType = null
        )
        updateExpenseInstance(emptyExpense)
    }
}