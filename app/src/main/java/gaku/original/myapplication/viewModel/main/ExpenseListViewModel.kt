package gaku.original.myapplication.viewModel.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.MONTH_RANGE
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.utility.AppTimeZone
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.viewModel.ExpenseSharedViewModel
import gaku.original.myapplication.viewModel.SharedImageViewModel
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
    private val tmpExpenseViewModel: TemporaryExpenseViewModel,
    private val sharedImageViewModel: SharedImageViewModel
) : ViewModel() {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    init {
        Log.d(className, "Init was called")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(className, "${className} was cleared!!")
    }

    /********************* MainView用*******************************/
    private var centerCalendarDate =
        AppTimeZone.getCurrentTimeInZone().toLocalDate()//こいつはmutableStateである必要はない

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

    /**
     * 過去の表示月と、現在時刻の表示
     */
    fun getPageDiffFromCenter() {

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

    private val _monthTotalExpense = MutableStateFlow(0L)
    val monthTotalExpense: StateFlow<Long> = _monthTotalExpense


    fun filterExpensesByMonth() {
        viewModelScope.launch {
            waitForInitialization()
            val targetYear = getCalendarYear()
            val targetMonth = getCalendarMonth()

            /**
             * UTCから設定のタイムゾーンに変えてフィルターする。
             * storedExpensesの段階ではISOで入っている。
             */
            Log.d("ExpenseListViewModel", "storedExpenses:${storedExpenses}↓")
            _filteredExpenses.value = expenseSharedViewModel.storedExpenses.value
                .filter { expense ->
                    LogAkitaDebug(
                        "expense:${expense.datetime} filtered:${
                            AppTimeZone.isoStringToLocalDateTime(
                                expense.datetime
                            )
                        }"
                    )
                    AppTimeZone.isoStringToLocalDateTime(expense.datetime)?.let { localDateTime ->
                        localDateTime.year == targetYear && localDateTime.monthValue == targetMonth
                    } ?: false
                }
                .sortedByDescending {
                    AppTimeZone.isoStringToLocalDateTime(it.datetime)
                }
            Log.d("ExpenseListViewModel", "filterExpensesByMonth was executed.↓")
            Log.d("ExpenseListViewModel", "${_filteredExpenses.value}")

            _monthTotalExpense.value =
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
        tmpExpenseViewModel.resetTmpExpenseList()
    }

    /**
     * カレンダーからAddEditする場合の関数
     * UI上で生成したDateTimeになるようなUTCを生成して、
     * それをExpenseのdatetimeにセットする。
     */
    fun setToTmpExpenseFromCalendar(newDateTimeZone: LocalDateTime) {
        /**
         * 入力は設定タイムゾーンの時間なので、UTCになおしてそれを
         */
        val utcStr = AppTimeZone.localDateTimeToIsoString(newDateTimeZone)
        tmpExpenseViewModel.resetTmpExpenseList()
        tmpExpenseViewModel.updateTmpExpense(
            tmpExpenseViewModel.currentTmpExpense.copy(
                //datetimeはStringなので注意!!! 変換必要
                datetime = utcStr
            )
        )
    }

    /***************** サインイン後特別 ***************************/
    fun onSignedIn(callback: (SuspendFuncStatusInfo) -> Unit) {
        /* サインイン時には、ユーザーが変わっているので、カレンダーのページも元に戻す */
        if (expenseSharedViewModel.isFirstSignIn.value) {
            /* サインイン時にやることがある */
            centerCalendarDate = AppTimeZone.getCurrentTimeInZone().toLocalDate()
            resetMonthOffset()
            expenseSharedViewModel.onSignedIn { statusInfo ->
                callback(statusInfo)
            }
            expenseSharedViewModel.setIsFirstSignIn(false)
        }
    }

    fun isShouldMoveToOCR(): Boolean {
        return sharedImageViewModel.isFromShareReceiver && !sharedImageViewModel.isMovedToOCR
    }

    fun setIsMovedToOCR(value: Boolean = true) {
        sharedImageViewModel.isMovedToOCR = value
    }
}