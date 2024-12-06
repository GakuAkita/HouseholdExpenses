package gaku.original.myapplication

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ServerValue.*
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.ExpenseRepository
import gaku.original.myapplication.data.idGeneration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/*
rememberの役割
状態の保存：composable関数がrecomposeされる際に状態を維持する。
これにより、ユーザーの操作や外部からのデータ更新によってUIが再描画されたときに、ユーザーが入力した値や選択したオプションがリセットされることがない

mutableStateOfの役割
リアクティブな状態の生成：mutableStateOfは値が変更されると自動的にその値を参照しているUIを再描画する

rememberとViewModelは違う。
 */

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository
):ViewModel() ,idGeneration{
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
    //なんでLiveDataかわからんけど、まあいずれわかってくるか。
    private val _userId = MutableLiveData<String>()
    val userId: LiveData<String> get() = _userId

    private val _allExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val allExpense:StateFlow<List<Expense>> get() = _allExpenses

    private val _filteredExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val filteredExpenses: StateFlow<List<Expense>> get() = _filteredExpenses


    val addObserveExpensesDoneFlag=MutableLiveData(false)
    var lastFetchedTime = System.currentTimeMillis()
    fun observeExpenses() {
        expenseRepository.observeExpenses(
            userId.value.toString(),
            lastFetchedTime = lastFetchedTime,
            onExpenseAdded = { newExpense ->
                viewModelScope.launch {
                    Log.d("ExpenseViewModel", "_allExpenses.value: ${_allExpenses.value.size}")
                    _allExpenses.value = _allExpenses.value + newExpense
                    Log.d("ExpenseViewModel", "Expense added: $newExpense")
                    //更新する
                    lastFetchedTime = System.currentTimeMillis()
                }
            },
            onExpenseUpdated = { updatedExpense ->
                viewModelScope.launch {
                    _allExpenses.value = _allExpenses.value.map { expense ->
                        if (expense.id == updatedExpense.id) updatedExpense else expense
                    }
                    Log.d("ExpenseViewModel", "Expense updated: $updatedExpense")
                }
            },
            onExpenseRemoved = { removedExpense ->
                viewModelScope.launch {
                    _allExpenses.value = _allExpenses.value.filterNot { expense ->
                        expense.id == removedExpense.id
                    }
                    Log.d("ExpenseViewModel", "Expense removed: $removedExpense")
                }
            }
        )
    }

    fun setUserId(id:String){
        _userId.value = id
    }

    fun addUserInitialData(email:String){
        viewModelScope.launch {
            expenseRepository.addUserInitialData(_userId.value.toString(),email)
        }
    }

    fun fetchAllExpenses(onComplete:()->Unit={}){
        viewModelScope.launch {
            _allExpenses.value = expenseRepository.fetchUserExpenses(_userId.value.toString())
            Log.d("ExpenseViewModel","Expenses:${_allExpenses.value}")
            onComplete()
        }
    }

    fun filterExpensesByMonth() {
        val targetYear = getCalendarYear()
        val targetMonth = getCalendarMonth()

        _filteredExpenses.value = _allExpenses.value.filter { expense ->
            val expenseDate = toLocalDateTime(expense.datetime)
            expenseDate?.year == targetYear && expenseDate.monthValue == targetMonth
        }
        Log.d("ExpenseViewModel","filterExpensesByMonth")
    }

    fun addExpense(expense: Expense,num:Int){
        //項目の中で中身がnullだとRealtime Databaseで何も入ってくれない
        if(expense.id == null){
            expense.id = generateExpenseId(num=0)
            expense.generatedType = "manual"
        }
        if(expense.category == null){
            expense.category = ""
        }
        if(expense.note == null){
            expense.note = ""
        }
        viewModelScope.launch {
            expenseRepository.addExpense(_userId.value.toString(),expense)
        }
    }

    /*
    A:AllExpensesを取って、それを月ごとに抽出
    B:前後12ヶ月分だけローカルに保存して、ローカルと変化があったときに同期させる
    C:
    */



    /********************* AddEditView用*******************************/
    // 初期値として null もしくは適切なデフォルト値を設定
    private val _expense = mutableStateOf<Expense>(
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

    fun getExpenseInstanceId():String?{
        return _expense.value.id
    }

    fun getExpenseInstanceAmount():Long?{
        return _expense.value.amount
    }

    fun getExpenseInstanceDateTime():LocalDateTime?{
        return toLocalDateTime(_expense.value.datetime)
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