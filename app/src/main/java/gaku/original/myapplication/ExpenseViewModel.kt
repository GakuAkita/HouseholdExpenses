package gaku.original.myapplication

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository = Graph.expenseRepository
):ViewModel() {
    /********************* MainView用*******************************/
    private val calendarDate = LocalDate.now()//こいつはmutableStateである必要はない

    private val _monthOffset = MutableStateFlow(0) // MutableStateFlowに変更
    val monthOffset: StateFlow<Int> = _monthOffset
    /*private val _monthOffset = mutableStateOf(0)
    val monthOffset: State<Int> = _monthOffset*/

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

    /* しばらくデータが溜まっていない限りはこれでいいか。 */
    private val _allExpenses=MutableStateFlow<List<Expense>>(emptyList())
    val allExpenses:StateFlow<List<Expense>> = _allExpenses

    private val _monthFilteredExpenses= MutableStateFlow<List<Expense>>(emptyList())
    val monthExpensesList: StateFlow<List<Expense>> = _monthFilteredExpenses

    init {
        viewModelScope.launch {
            expenseRepository.getAllExpenses().collect { expenses->
                _allExpenses.value=expenses
                updateMonthFilteredExpenses(expenses)
            }
        }

        viewModelScope.launch {
            // monthOffset の変更を監視して処理をトリガー
            _monthOffset.collect { offset ->
                updateMonthFilteredExpenses(_allExpenses.value)
            }
        }
    }

    private fun updateMonthFilteredExpenses(expenses: List<Expense>) {
        val year = getCalendarYear()
        val month = getCalendarMonth()

        _monthFilteredExpenses.value = expenses.filter { expense ->
            expense.datetime.year == year && expense.datetime.monthValue == month
        }
    }

    /*
    A:AllExpensesを取って、それを月ごとに抽出
    B:前後12ヶ月分だけローカルに保存して、ローカルと変化があったときに同期させる
    C:
    */

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