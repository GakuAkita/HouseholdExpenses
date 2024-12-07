package gaku.original.myapplication

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.ExpenseRepository
import gaku.original.myapplication.data.SignInStatus
import gaku.original.myapplication.data.SignUpStatus
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
    private val expenseRepository: ExpenseRepository,
    private val sharedViewModel:SharedViewModel
):ViewModel(){
    /** ユーザー情報 **/
    fun updateUserId(id:String?){
        sharedViewModel.setUserId(id)
    }

    fun getUserId():String?{
        return sharedViewModel.userId.value
    }

    fun signUpAndInitialSetup(email:String,password:String,callback:(SignUpStatus)->Unit){
        sharedViewModel.signUp(
            email=email,
            password = password,
            callback = { status ->
                when (status){
                    SignUpStatus.SUCCESS -> {
                        callback(SignUpStatus.SUCCESS)
                    }
                    SignUpStatus.USER_ID_NULL->{
                        //UI側でToastする内容を変えたいのでこんな入れ子構造に。
                        callback(SignUpStatus.USER_ID_NULL)
                    }
                    SignUpStatus.SIGN_UP_FAILED ->{
                        callback(SignUpStatus.SIGN_UP_FAILED)
                    }
                }
            }
        )
    }

    fun signInAndFetchAllExpenses(email:String,password:String,callback: (SignInStatus) -> Unit){
        sharedViewModel.signIn(
            email = email,
            password = password,
            callback = {status ->
                when (status){
                    SignInStatus.SUCCESS -> {
                        //このフラグがたっているときはサインアップ後のログイン
                        if(sharedViewModel.isAfterSignUp.value == true){
                            addUserInitialData(email)
                            sharedViewModel.isAfterSignUp.value = false
                        }
                        fetchAllExpenses(
                            onComplete = {
                                filterExpensesByMonth()
                            }
                        )
                        callback(SignInStatus.SUCCESS)
                    }
                    SignInStatus.USER_ID_NULL->{
                        callback(SignInStatus.USER_ID_NULL)
                    }
                    SignInStatus.SIGN_IN_FAILED ->{
                        callback(SignInStatus.SIGN_IN_FAILED)
                    }
                }
            }
        )
    }

    /*********************デバイス管理********************************/
    val deviceId: LiveData<String> = sharedViewModel.deviceId

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

    private val _allExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val allExpense:StateFlow<List<Expense>> get() = _allExpenses

    private val _filteredExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val filteredExpenses: StateFlow<List<Expense>> get() = _filteredExpenses

    /***** タイムスタンプの管理 *****/
    fun getLastFetchedTime(deviceId:String){
        expenseRepository(
            getUserId(),
            deviceId,
            callback = {
                lastFetchedTime = it?:0L
            }
        )
    }

    var lastFetchedTime<Long?> = null

    val addObserveExpensesDoneFlag=MutableLiveData(false)
    fun observeExpenses() {
        expenseRepository.observeExpenses(
            sharedViewModel.userId.value.toString(),
            lastFetchedTime = lastFetchedTime,
            onExpenseAdded = { newExpense ->
                viewModelScope.launch {
                    Log.d("ExpenseViewModel", "_allExpenses.value size: ${_allExpenses.value.size}")
                    _allExpenses.value += newExpense
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

    fun addUserInitialData(email:String){
        viewModelScope.launch {
            expenseRepository.addUserInitialData(getUserId()?:"empty",email)
            Log.d("ExpenseViewModel","addUserInitialData for ${getUserId()}")
        }
    }

    fun fetchAllExpenses(onComplete:()->Unit={}){
        viewModelScope.launch {
            _allExpenses.value = expenseRepository.fetchUserExpenses(getUserId()?:"empty")
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

    fun addExpense(expense: Expense){
        //idはpushしたときに代入することにする。したがって、nullのままにする。
        //repositoryのaddExpenseでidを格納する
        if(expense.category == null){
            expense.category = ""
        }
        if(expense.note == null){
            expense.note = ""
        }
        viewModelScope.launch {
            expenseRepository.addExpense(getUserId()?:"",expense)
        }
    }

    fun updateExpense(expense:Expense){
        viewModelScope.launch {
            expenseRepository.updateExpense(getUserId()?:"",expense)
        }
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