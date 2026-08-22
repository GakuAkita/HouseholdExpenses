package gaku.original.myapplication.ui.screens.bottom.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.common.AppError
import gaku.original.myapplication.common.AppResult
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.appTimeZone.toInstant
import gaku.original.myapplication.data.repository.appTimeZone.toLocalDateTime
import gaku.original.myapplication.data.repository.expense.ExpenseQuery
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

data class ExpenseUi(
    val id: String?,
    val amount: Long?,
    val datetime: LocalDateTime?,
    val category: Category?
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val selectedMonth: YearMonth = YearMonth.now(),
    val shownExpenses: List<ExpenseUi> = emptyList(),
    val monthlyTotal: Long = 0L,
    val dailyAmounts: Map<LocalDate, Long> = emptyMap()
)

sealed interface ExpenseEditError : AppError {
    data object IdEmpty : ExpenseEditError {
        override val message: String
            get() = "id is empty"
    }
}

class HomeViewModel(
    private val expenseRepository: ExpenseRepository,
    private val appTimeZoneRepository: AppTimeZoneRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState get() = _uiState.asStateFlow()

    private var lastQuery = ExpenseQuery()
    private var cachedExpenses = emptyMap<String, Expense>()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val expenseRepository = app.appContainer.sessionContainer!!.expenseRepository
                val appTimeZoneRepository =
                    app.appContainer.sessionContainer!!.appTimeZoneRepository
                HomeViewModel(expenseRepository, appTimeZoneRepository)
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")

        appTimeZoneRepository.startListening()

        viewModelScope.launch {
            appTimeZoneRepository.zoneId.collect {
                /* reorganize the expenses list based on the new zoneId */
                Timber.d("ZoneId was updated!")
            }
        }

        refreshExpenses(YearMonth.now())
        viewModelScope.launch {
            expenseRepository.expenses.collect { expenses ->
                cachedExpenses = expenses
                rebuildExpenseUiState()
            }
        }
    }

    fun rebuildExpenseUiState() {
        val zoneId = appTimeZoneRepository.zoneId.value
        /* filter only selected month */
        val expenseUiList = cachedExpenses.values.filter {
            it.datetime?.toLocalDateTime(zoneId)?.monthValue == _uiState.value.selectedMonth.monthValue
        }.map { it.toUi(zoneId) }

        /* calculate statistics and each day amount */
        val monthlyTotal = expenseUiList.sumOf { it.amount ?: 0L }
        val dailyAmounts = expenseUiList.groupBy { it.datetime!!.toLocalDate() }
            .mapValues { (_, expenses) ->
                expenses.sumOf { it.amount ?: 0L }
            }
        _uiState.update {
            it.copy(
                shownExpenses = expenseUiList,
                monthlyTotal = monthlyTotal,
                dailyAmounts = dailyAmounts
            )
        }
    }

    /* onMonthChanged is definitely called once when the screen is created. */
    fun onMonthChanged(month: YearMonth) {
        Timber.d("Swiped to ${month.year}-${month.monthValue} hash=${hashCode()}");
        _uiState.update {
            it.copy(
                selectedMonth = month,
            )
        }

        /* this should be called after uiState selectedMonth is updated */
        rebuildExpenseUiState()

        val lastQueryStart = lastQuery.datetimeFrom
        val lastQueryEnd = lastQuery.datetimeTo

        if (lastQueryStart != null && lastQueryEnd != null) {
            val twoDaysLaterStart = lastQueryStart.plus(Duration.ofDays(2))
            val twoDaysBeforeEnd = lastQueryEnd.minus(Duration.ofDays(2))

            val fromMonth = twoDaysLaterStart?.atZone(ZoneId.of("UTC"))?.monthValue
            val toMonth = twoDaysBeforeEnd?.atZone(ZoneId.of("UTC"))?.monthValue

            if (_uiState.value.selectedMonth.monthValue < fromMonth!! ||
                _uiState.value.selectedMonth.monthValue > toMonth!!
            ) {
                refreshExpenses(_uiState.value.selectedMonth)
            }
        }
    }

    fun onExpenseClick(id: String?): AppResult<Expense?, ExpenseEditError> {
        if (id == null || cachedExpenses[id] == null) {
            _uiState.update {
                it.copy(
                    message = "id should not be null. This is a coding error"
                )
            }
            return AppResult.Failure(ExpenseEditError.IdEmpty)
        }

        val expense = cachedExpenses[id]
        return AppResult.Success(expense)
    }


    fun refreshExpenses(
        month: YearMonth
    ) {
        val zoneId = appTimeZoneRepository.zoneId.value

        /* monitor from the first day of 2 months ago to the end day of 2 months later */
        val startMonth = month.minusMonths(2)
        val endMonth = month.plusMonths(2)

        val startDateTime = startMonth.atDay(1).atStartOfDay().toInstant(zoneId)
        /* the start of the first day of the next month */
        val endDateTime = endMonth.plusMonths(1).atDay(1).atStartOfDay().toInstant(zoneId)

        val query = ExpenseQuery(
            datetimeFrom = startDateTime,
            datetimeTo = endDateTime
        )

        Timber.d("Refresh Expenses: start=${startDateTime} end=${endDateTime} zoneId=${zoneId}")
        viewModelScope.launch {
            expenseRepository.stopListening()
            expenseRepository.startListening(query)
            lastQuery = query
        }
    }

    override fun onCleared() {
        Timber.d("onCleared called. ${hashCode()}")
        expenseRepository.stopListening()
        appTimeZoneRepository.stopListening()
        super.onCleared()
    }
}

fun Expense.toUi(zoneId: ZoneId): ExpenseUi = ExpenseUi(
    id = id,
    amount = amount,
    datetime = datetime?.toLocalDateTime(zoneId),
    category = category
)