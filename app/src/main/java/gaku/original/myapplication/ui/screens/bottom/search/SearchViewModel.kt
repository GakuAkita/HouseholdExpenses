package gaku.original.myapplication.ui.screens.bottom.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.ExpenseSearchFilter
import gaku.original.myapplication.data.dataClass.GeneratedTypeKind
import gaku.original.myapplication.data.dataClass.kind
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.appTimeZone.toInstant
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.expense.ExpenseQuery
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID

/**
 * How far back to search. Firestore cannot do partial matches or combine
 * these conditions with a datetime range without composite indexes,
 * so only the period is queried and the rest is filtered on the device.
 */
enum class SearchPeriod(val label: String, val months: Long?) {
    ThreeMonths("3 months", 3),
    OneYear("1 year", 12),
    All("All", null),
}

data class SearchResultUi(
    val id: String,
    val datetime: LocalDateTime,
    val amount: Long,
    val categoryName: String?,
    val storeName: String?,
    val itemName: String?,
    val note: String?,
    val generatedType: GeneratedTypeKind,
)

data class SearchUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val filter: ExpenseSearchFilter = ExpenseSearchFilter.Uncategorized,
    val period: SearchPeriod = SearchPeriod.ThreeMonths,
    val categories: List<Category> = emptyList(),
    val results: List<SearchResultUi> = emptyList(),
    val total: Long = 0L,
)

class SearchViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val appTimeZoneRepository: AppTimeZoneRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState get() = _uiState.asStateFlow()

    private var subscriptionId: String? = null
    private var cachedExpenses = emptyMap<String, Expense>()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val sessionContainer = app.appContainer.sessionContainer!!
                SearchViewModel(
                    sessionContainer.expenseRepository,
                    sessionContainer.categoryRepository,
                    sessionContainer.appTimeZoneRepository
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")

        refreshExpenses(_uiState.value.period)

        viewModelScope.launch {
            expenseRepository.expenses.collect { allExpenses ->
                val expenses = allExpenses[subscriptionId]
                /* No entry yet means the first snapshot has not arrived. */
                _uiState.update { it.copy(isLoading = expenses == null) }
                cachedExpenses = expenses ?: emptyMap()
                rebuildResults()
            }
        }

        viewModelScope.launch {
            try {
                categoryRepository.categories.collect { categories ->
                    _uiState.update {
                        it.copy(categories = categories.values.sortedBy { category -> category.timestamp })
                    }
                    rebuildResults()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Failed to fetch categories.:${e.message}") }
            }
        }

        viewModelScope.launch {
            appTimeZoneRepository.zoneId.collect {
                rebuildResults()
            }
        }
    }

    fun onKeywordChanged(keyword: String) {
        updateFilter { it.copy(keyword = keyword) }
    }

    /* null is the "Uncategorized" chip. */
    fun onCategoryToggled(categoryId: String?) {
        updateFilter { it.copy(categoryIds = it.categoryIds.toggle(categoryId)) }
    }

    fun onGeneratedTypeToggled(kind: GeneratedTypeKind) {
        updateFilter { it.copy(generatedTypes = it.generatedTypes.toggle(kind)) }
    }

    fun onClearFilter() {
        updateFilter { ExpenseSearchFilter() }
    }

    fun onPeriodSelected(period: SearchPeriod) {
        if (period == _uiState.value.period) return
        _uiState.update { it.copy(period = period) }
        refreshExpenses(period)
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    fun getExpense(id: String): Expense? {
        val expense = cachedExpenses[id]
        if (expense == null) {
            _uiState.update { it.copy(message = "The expense was not found. It may have been deleted.") }
        }
        return expense
    }

    private fun updateFilter(transform: (ExpenseSearchFilter) -> ExpenseSearchFilter) {
        _uiState.update { it.copy(filter = transform(it.filter)) }
        rebuildResults()
    }

    private fun rebuildResults() {
        val state = _uiState.value
        val zoneId = appTimeZoneRepository.zoneId.value
        /* Keep the name the expense was saved with when the category was deleted. */
        val categoryNames = state.categories.associate { it.id to it.name }

        val matched = cachedExpenses.values
            .filter { state.filter.matches(it) }
            .sortedByDescending { it.datetime }

        val results = matched.map { expense ->
            SearchResultUi(
                id = expense.id,
                datetime = expense.datetime.atZone(zoneId).toLocalDateTime(),
                amount = expense.amount,
                categoryName = expense.category?.let { categoryNames[it.id] ?: it.name },
                storeName = expense.storeName,
                itemName = expense.itemName,
                note = expense.note,
                generatedType = expense.generatedType.kind(),
            )
        }

        _uiState.update {
            it.copy(
                results = results,
                total = matched.sumOf { expense -> expense.amount }
            )
        }
    }

    private fun refreshExpenses(period: SearchPeriod) {
        val zoneId = appTimeZoneRepository.zoneId.value
        /* Include the whole oldest month. No upper bound, so that future expenses are found too. */
        val from = period.months?.let {
            YearMonth.now(zoneId).minusMonths(it - 1).atDay(1).atStartOfDay().toInstant(zoneId)
        }
        val query = ExpenseQuery(datetimeFromOrEqual = from)

        Timber.d("Refresh Expenses: period=$period from=$from")
        subscriptionId?.let { expenseRepository.stopListening(it) }
        cachedExpenses = emptyMap()
        _uiState.update { it.copy(isLoading = true) }

        try {
            subscriptionId = UUID.randomUUID().toString()
            expenseRepository.startListening(subscriptionId!!, query)
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, message = e.message) }
        }
        rebuildResults()
    }

    override fun onCleared() {
        Timber.d("onCleared called. ${hashCode()}")
        subscriptionId?.let { expenseRepository.stopListening(it) }
        super.onCleared()
    }
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value
