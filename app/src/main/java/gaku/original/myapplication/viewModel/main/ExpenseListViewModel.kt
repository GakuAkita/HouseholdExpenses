package gaku.original.myapplication.viewModel.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.AppTimeZone
import gaku.original.myapplication.data.Constants.MONTH_RANGE
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.GeneratedType
import gaku.original.myapplication.viewModel.shared.ExpenseSharedViewModel
import gaku.original.myapplication.viewModel.shared.SharedImageViewModel
import gaku.original.myapplication.viewModel.shared.SharedNotificationListenerViewModel
import gaku.original.myapplication.viewModel.shared.TemporaryExpenseViewModel
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
    private val sharedImageViewModel: SharedImageViewModel,
    private val sharedNotificationListenerViewModel: SharedNotificationListenerViewModel
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
        callback: (FuncStatusInfo) -> Unit
    ) {
        /* カテゴリーはクリアしない！！ */
        expenseSharedViewModel.clearAllExpenses()
        expenseSharedViewModel.clearAllListeners()
        fetchMonthsExpensesInternal(currentPageYear, currentPageMonth, callback = { status ->
            if (status.status == FuncStatus.SUCCESS) {
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
        callback: (FuncStatusInfo) -> Unit
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

    fun calcMonthTotalExpense() {
        _monthTotalExpense.value =
            _filteredExpenses.value.sumOf { expense -> (expense.amount ?: 0).toLong() }
    }

    private val _monthlyEstimatedExpense = MutableStateFlow<Long?>(0L)
    val monthlyEstimatedExpense: StateFlow<Long?> = _monthlyEstimatedExpense
    fun calcMonthlyEstimatedExpense() {
        /**
         * monthOffsetが0以外のときも計算されるが、
         * estimatedが基本0に入る。UI上には表示しないから問題ない。
         */

        /**
         * 1.月の初日から今日までの支出の合計[A]を計算する
         * 2.月の初日から今日までの中から繰り返し追加のものだけを省いた合計[B]を計算。
         * この[B]が普通にかかる通常かかる費用
         * 3.明日以降はすでに追加されている費用(繰り返し追加含む)に通常費用([B]/今日までの日付)がかかるとして、月の予想合計を出す
         */
        val today = AppTimeZone.getCurrentTimeInZone().toLocalDate()

        val startOfMonth = AppTimeZone.getCurrentTimeInZone().withDayOfMonth(1).toLocalDate()
        val endOfMonth = YearMonth.of(today.year, today.monthValue).atEndOfMonth()

        val expensesByToday = _filteredExpenses.value
            .filter { expense ->
                AppTimeZone.isoStringToLocalDateTime(expense.datetime)?.toLocalDate()
                    ?.let { date -> !date.isBefore(startOfMonth) && !date.isAfter(today) }
                    ?: false
            }
        val expensesFromTodayToEnd = _filteredExpenses.value
            .filter { expense ->
                AppTimeZone.isoStringToLocalDateTime(expense.datetime)?.toLocalDate()
                    ?.let { date -> date.isAfter(today) && !date.isAfter(endOfMonth) }
                    ?: false
            }
        val sumByToday = expensesByToday.sumOf { it.amount ?: 0L }

        val sumByTodayExcludingRepeatAddAndTooBig =
            expensesByToday.filterNot { expense ->
                expense.generatedType?.startsWith(GeneratedType.REPEAT_ADD) ?: false
            }.filter { expense ->
                /* あまりにでかい金額はなにか高いものを買ったせいなので、省いていおく */
                (expense.amount ?: 0L) < 10000L /* 10000円のものを毎週買うことはないだろう。 */
            }.sumOf { it.amount ?: 0L }

        val daysFromStartToToday = (today.dayOfMonth) // 1日から今日までの日数
        /* 一日にどれだけ金を使うか。(繰り返し追加は抜き) */
        val everydayAmount = sumByTodayExcludingRepeatAddAndTooBig / daysFromStartToToday

        /* 明日以降の金額の合計 */
        val sumFromTodayToEndAlready = expensesFromTodayToEnd.sumOf { it.amount ?: 0L }

        val estimatedTotal = sumByToday + sumFromTodayToEndAlready +
                (everydayAmount * (endOfMonth.dayOfMonth - today.dayOfMonth))
        _monthlyEstimatedExpense.value = estimatedTotal
    }

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
                    AppTimeZone.isoStringToLocalDateTime(expense.datetime)?.let { localDateTime ->
                        localDateTime.year == targetYear && localDateTime.monthValue == targetMonth
                    } ?: false
                }
                .sortedByDescending {
                    AppTimeZone.isoStringToLocalDateTime(it.datetime)
                }
            Log.d("ExpenseListViewModel", "filterExpensesByMonth was executed.↓")
            Log.d("ExpenseListViewModel", "${_filteredExpenses.value}")

            calcMonthTotalExpense()
            calcMonthlyEstimatedExpense()
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
    fun onSignedIn(callback: (FuncStatusInfo) -> Unit) {
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
        /* OCR画面に移動したらDataをnullに設定する */
        return !sharedImageViewModel.isMovedToOCR && sharedImageViewModel.sharedImageData.value != null
    }

    fun setIsMovedToOCR(value: Boolean = true) {
        sharedImageViewModel.isMovedToOCR = value
    }

    fun isShouldMoveToNLProcess(): Boolean {
        return !sharedNotificationListenerViewModel.isMovedToNLProcess && sharedNotificationListenerViewModel.getNotificationData() != null
    }

    fun setIsMovedToNLProcess(value: Boolean = true) {
        sharedNotificationListenerViewModel.isMovedToNLProcess = value
    }
}