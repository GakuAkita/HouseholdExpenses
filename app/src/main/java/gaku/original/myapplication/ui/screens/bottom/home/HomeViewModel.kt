package gaku.original.myapplication.ui.screens.bottom.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.appTimeZone.toInstant
import gaku.original.myapplication.data.repository.appTimeZone.toIsoUtcString
import gaku.original.myapplication.data.repository.appTimeZone.toLocalDateTime
import gaku.original.myapplication.data.repository.expense.ExpenseQuery
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.stream.Collectors.toList

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
)

class HomeViewModel(
    private val expenseRepository: ExpenseRepository,
    private val appTimeZoneRepository: AppTimeZoneRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val initialMonth = YearMonth.now()
    private var cachedExpenses:Map<String, Expense> = emptyMap()

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


        refreshExpenses(initialMonth)
        viewModelScope.launch {
            expenseRepository.expenses.collect { expenses->
                /* filter only selected month */

            }
        }

        appTimeZoneRepository.startListening()

        viewModelScope.launch {
            appTimeZoneRepository.zoneId.collect {
                /* reorganize the expenses list based on the new zoneId */
                Timber.d("ZoneId was updated!")
            }
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
    }

    fun refreshExpenses(
        month: YearMonth
    ){
        val zoneId = appTimeZoneRepository.zoneId.value

        /* monitor from the first day of 2 months ago to the end day of 2 months later */
        val startMonth = month.minusMonths(2)
        val endMonth = month.plusMonths(2)

        val startDateTimeStr = startMonth.atDay(1).atStartOfDay().toInstant(zoneId)
        val endDateTimeStr = endMonth.atDay(1).atStartOfDay().toInstant(zoneId)

        val query = ExpenseQuery(
            datetimeFrom = startDateTimeStr,
            datetimeTo = endDateTimeStr
        )
        viewModelScope.launch {
            expenseRepository.stopListening()
            expenseRepository.startListening(query)
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
    id=id,
    amount = amount,
    datetime = datetime?.toLocalDateTime(zoneId),
    category = category
)