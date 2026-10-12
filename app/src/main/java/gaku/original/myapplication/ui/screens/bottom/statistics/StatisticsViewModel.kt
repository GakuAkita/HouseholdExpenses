package gaku.original.myapplication.ui.screens.bottom.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.appTimeZone.toInstant
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.expense.ExpenseQuery
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.coroutines.cancellation.CancellationException

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val selectedMonth: YearMonth = YearMonth.now(),
    /* null until the months it needs are read. */
    val summary: MonthSummary? = null,
    val trend: CategoryTrend? = null,
    /* The app time zone, which expense dates are shown in. */
    val zoneId: ZoneId = ZoneId.systemDefault(),
)

class StatisticsViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val appTimeZoneRepository: AppTimeZoneRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    /*
     * Expenses of each month in the app time zone. Expenses are read once per request, not listened to.
     * A month is kept until a refresh, so moving to another month reads only the months not read yet.
     */
    private val expensesByMonth = mutableMapOf<YearMonth, List<Expense>>()
    private var fetchJob: Job? = null

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val sessionContainer = app.appContainer.sessionContainer!!
                StatisticsViewModel(
                    sessionContainer.expenseRepository,
                    sessionContainer.categoryRepository,
                    sessionContainer.appTimeZoneRepository
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")

        viewModelScope.launch {
            try {
                categoryRepository.categories.collect { rebuild() }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Failed to fetch categories.:${e.message}") }
            }
        }

        /* Month boundaries depend on the time zone, so a new time zone reads again. This also does the first read. */
        viewModelScope.launch {
            appTimeZoneRepository.zoneId.collect {
                expensesByMonth.clear()
                loadSelectedMonth()
            }
        }
    }

    fun onPreviousMonth() {
        _uiState.update { it.copy(selectedMonth = it.selectedMonth.minusMonths(1)) }
        loadSelectedMonth()
    }

    fun onNextMonth() {
        _uiState.update { it.copy(selectedMonth = it.selectedMonth.plusMonths(1)) }
        loadSelectedMonth()
    }

    fun onRefresh() {
        expensesByMonth.clear()
        loadSelectedMonth()
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    /** Reads the months of the selected month's trend that have not been read yet. */
    private fun loadSelectedMonth() {
        val zoneId = appTimeZoneRepository.zoneId.value
        val missing = StatisticsCalculator.trendMonths(_uiState.value.selectedMonth)
            .filter { it !in expensesByMonth }

        rebuild()
        if (missing.isEmpty()) return

        /* One range query from the oldest to the newest missing month. Months in between are read again. */
        val firstMonth = missing.first()
        val lastMonth = missing.last()
        val query = ExpenseQuery(
            datetimeFromOrEqual = firstMonth.atDay(1).atStartOfDay().toInstant(zoneId),
            datetimeTo = lastMonth.plusMonths(1).atDay(1).atStartOfDay().toInstant(zoneId),
        )
        Timber.d("Load expenses: $firstMonth to $lastMonth")

        /* Only the latest request may set the results. */
        fetchJob?.cancel()
        _uiState.update { it.copy(isLoading = true) }

        fetchJob = viewModelScope.launch {
            try {
                val byMonth = expenseRepository.getExpenses(query)
                    .groupBy { StatisticsCalculator.monthOf(it, zoneId) }
                var month = firstMonth
                while (month <= lastMonth) {
                    expensesByMonth[month] = byMonth[month].orEmpty()
                    month = month.plusMonths(1)
                }
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.update { it.copy(isLoading = false, message = "Failed to fetch expenses.:${e.message}") }
            }
            rebuild()
        }
    }

    private fun rebuild() {
        val month = _uiState.value.selectedMonth
        val months = StatisticsCalculator.trendMonths(month)

        if (months.any { it !in expensesByMonth }) {
            _uiState.update { it.copy(summary = null, trend = null) }
            return
        }

        val zoneId = appTimeZoneRepository.zoneId.value
        val categoryNames = categoryRepository.categories.value.mapValues { it.value.name }

        val summary = StatisticsCalculator.summarize(
            month = month,
            expenses = expensesByMonth.getValue(month),
            previousMonthExpenses = expensesByMonth.getValue(month.minusMonths(1)),
            categoryNames = categoryNames,
            today = LocalDate.now(zoneId),
        )
        val trend = StatisticsCalculator.trend(months, expensesByMonth, categoryNames)

        _uiState.update { it.copy(summary = summary, trend = trend, zoneId = zoneId) }
    }

    override fun onCleared() {
        Timber.d("Cleared ${hashCode()}")
        super.onCleared()
    }
}
