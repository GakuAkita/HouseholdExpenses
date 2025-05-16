package gaku.original.myapplication.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.Utility.fromLocalDateTime
import gaku.original.myapplication.Utility.toLocalDateTime
import gaku.original.myapplication.data.Constants.MONTH_RANGE
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

/*
rememberの役割
状態の保存：composable関数がrecomposeされる際に状態を維持する。
これにより、ユーザーの操作や外部からのデータ更新によってUIが再描画されたときに、ユーザーが入力した値や選択したオプションがリセットされることがない

mutableStateOfの役割
リアクティブな状態の生成：mutableStateOfは値が変更されると自動的にその値を参照しているUIを再描画する

rememberとViewModelは違う。
 */

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val expenseSharedViewModel: ExpenseSharedViewModel,
    private val tmpExpenseViewModel: TemporaryExpenseViewModel
) : ViewModel() {
    /********************* MainView用*******************************/
    private var centerCalendarDate = LocalDate.now()//こいつはmutableStateである必要はない

    private val _monthOffset = MutableStateFlow(0) // MutableStateFlowに変更
    val monthOffset: StateFlow<Int> = _monthOffset

    fun getCalendarYear(): Int {
        return centerCalendarDate.plusMonths(monthOffset.value.toLong()).year
    }

    fun getCalendarMonth(): Int {
        return centerCalendarDate.plusMonths(monthOffset.value.toLong()).monthValue
    }


    fun resetMonthOffset() {
        _monthOffset.value = 0
    }

    fun updateCenterCalendarDate(year: Int, month: Int) {
        centerCalendarDate = LocalDate.of(year, month, 1)
    }

    fun incrementMonth() {
        _monthOffset.value++
    }

    fun decrementMonth() {
        _monthOffset.value--
    }

    /********* Expense配列の管理 ***********/
    val storedExpenses: List<Expense> get() = expenseSharedViewModel.storedExpenses.value
    val expensesLoadingStatus: StateFlow<LoadingStatus> get() = expenseSharedViewModel.expensesLoadingStatus

    /**
     * カレンダーのページが行き過ぎたときはローカルで保持しているExpensesを更新する
     */
    fun updateStoredExpenses(
        currentPageYear: Int,
        currentPageMonth: Int,
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        /* カテゴリーはクリアしない！！ */
        expenseSharedViewModel.clearAllExpenses()
        expenseSharedViewModel.clearAllListeners()
        fetchMonthsExpensesInternal(currentPageYear, currentPageMonth, callback = { status ->
            if (status.status == SuspendFuncStatus.SUCCESS) {
                /* 成功のときのみ、リスナーを追加 */
                expenseSharedViewModel.addAllListeners(
                    yearMonth = YearMonth.of(
                        currentPageYear,
                        currentPageMonth
                    )
                )
            }
            callback(status)
        })
    }

    private fun fetchMonthsExpensesInternal(
        currentPageYear: Int,
        currentPageMonth: Int,
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        val fromMonth =
            YearMonth.of(currentPageYear, currentPageMonth).minusMonths(MONTH_RANGE)
        val toMonth =
            YearMonth.of(currentPageYear, currentPageMonth).plusMonths(MONTH_RANGE)
        /* SharedViewModel内で非同期処理をする。ここでは呼び出すだけ */
        expenseSharedViewModel.fetchMonthsExpenses(
            fromMonth,
            toMonth,
            callback = callback
        )
    }

    init {
        /* なんでこれ必要なんだ？ */
        observeStoredExpenses()
    }

    //allExpenseが更新されたときに
    private fun observeStoredExpenses() {
        viewModelScope.launch {
            expenseSharedViewModel.storedExpenses.collectLatest {
                filterExpensesByMonth()
            }
        }
    }

    private val _filteredExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val filteredExpenses: StateFlow<List<Expense>> get() = _filteredExpenses

    suspend fun waitForInitialization() {
        /* nullのまま.valueをするとクラッシュするので、待たせる */
        while (filteredExpenses == null || storedExpenses == null) {
            Log.d("ExpenseListViewModel", "Waiting for initialization...")
            delay(100) // 100msごとに再確認
        }
        //Log.d("ExpenseListViewModel", "Initialization complete.")
    }

    private val _monthTotal = MutableStateFlow(0L)
    val monthTotal: StateFlow<Long> = _monthTotal


    fun filterExpensesByMonth() {
        viewModelScope.launch {
            waitForInitialization()
            val targetYear = getCalendarYear()
            val targetMonth = getCalendarMonth()

            _filteredExpenses.value = expenseSharedViewModel.storedExpenses.value
                .filter { expense ->
                    val expenseYear = toLocalDateTime(expense.datetime)?.year ?: 0
                    val expenseMonth = toLocalDateTime(expense.datetime)?.monthValue ?: 0
                    expenseYear == targetYear && expenseMonth == targetMonth
                }
                .sortedByDescending {
                    it.datetime
                }
            Log.d("ExpenseListViewModel", "filterExpensesByMonth was executed.↓")
            Log.d("ExpenseListViewModel", "${_filteredExpenses.value}")

            _monthTotal.value =
                _filteredExpenses.value.sumOf { expense -> (expense.amount ?: 0).toLong() }
        }
    }

    /*
    A:storedExpensesを取って、それを月ごとに抽出
    B:前後12ヶ月分だけローカルに保存して、ローカルと変化があったときに同期させる
    C:
    */

    /** AddEditに値を渡す用 **/
    fun setToTmpExpense(expense: Expense) {
        tmpExpenseViewModel.updateTmpExpense(expense)
    }

    fun resetTmpExpense() {
        tmpExpenseViewModel.resetTmpExpense()
    }

    /* カレンダーからAddEditする場合の関数。日付だけ変えて他は初期値 */
    fun setToTmpExpenseFromCalendar(newDateTime: LocalDateTime) {
        tmpExpenseViewModel.resetTmpExpense()
        tmpExpenseViewModel.updateTmpExpense(
            tmpExpenseViewModel.tmpExpense.value.copy(
                //datetimeはStringなので注意!!! 変換必要
                datetime = fromLocalDateTime(newDateTime)
            )
        )
    }

    suspend fun onSignedIn(callback: (SuspendFuncStatusInfo) -> Unit) {
        /* SharedViewModelのviewModelScopeで処理 */
        val ret = expenseSharedViewModel.onSignedIn(callback)
    }
}