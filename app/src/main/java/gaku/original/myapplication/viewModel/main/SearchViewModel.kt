package gaku.original.myapplication.viewModel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.ExpenseSearchFilter
import gaku.original.myapplication.data.dataClass.getDefaultSearchFilter
import gaku.original.myapplication.repository.FirestoreRepository.ExpenseFirestoreRepository
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.viewModel.shared.TemporaryExpenseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val expenseFirestoreRepository: ExpenseFirestoreRepository,
    private val tmpExpenseViewModel: TemporaryExpenseViewModel,
) : ViewModel() {
    override fun onCleared() {
        /**
         * 他のボトムバーに移動したときにViewModelは破棄される
         */
        super.onCleared()
        LogAkitaDebug("${this::class.simpleName} Cleared!!!!")
    }

    private val _loadingStatus = MutableStateFlow(LoadingStatus.COMPLETED)
    val loadingStatus: StateFlow<LoadingStatus> get() = _loadingStatus

    private val _searchedExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val searchedExpenses: StateFlow<List<Expense>> get() = _searchedExpenses

    // 現在のフィルター条件（デフォルトはカテゴリーがnull）
    private val _currentFilter = MutableStateFlow(getDefaultSearchFilter())
    val currentFilter: StateFlow<ExpenseSearchFilter> get() = _currentFilter

    /**
     * フィルター条件を更新
     */
    fun updateFilter(filter: ExpenseSearchFilter) {
        _currentFilter.value = filter
    }

    /**
     * フィルター条件をリセット（カテゴリーがnullのデフォルト）
     */
    fun resetFilter() {
        _currentFilter.value = getDefaultSearchFilter()
    }

    /**
     * 現在のフィルター条件で検索を実行
     */
    private suspend fun searchExpensesInternal(): FuncResultWithData<List<Expense>> {
        _loadingStatus.value = LoadingStatus.LOADING
        val result = expenseFirestoreRepository.searchExpenses(_currentFilter.value)
        if (result is FuncResultWithData.Success) {
            _loadingStatus.value = LoadingStatus.COMPLETED
            _searchedExpenses.value = result.data
            LogAkitaDebug("expenses:${_searchedExpenses.value}")
        } else {
            if (result is FuncResultWithData.Failure.Timeout) {
                _loadingStatus.value = LoadingStatus.TIMEOUT
            } else {
                _loadingStatus.value = LoadingStatus.ERROR
            }
        }
        return result
    }

    /**
     * 検索を実行（公開API）
     */
    fun searchExpenses(callback: (FuncStatusInfo) -> Unit) {
        viewModelScope.launch {
            val result = searchExpensesInternal()
            callback(result.toFuncStatusInfo())
        }
    }

    /**
     * 特定のフィルターで検索を実行
     */
    fun searchWithFilter(filter: ExpenseSearchFilter, callback: (FuncStatusInfo) -> Unit) {
        updateFilter(filter)
        searchExpenses(callback)
    }

    // 後方互換性のため残す（将来的に削除予定）
    @Deprecated("Use searchExpenses() instead", ReplaceWith("searchExpenses(callback)"))
    private suspend fun fetchNotCategorizedExpensesInternal(): FuncResultWithData<List<Expense>> {
        _loadingStatus.value = LoadingStatus.LOADING
        val result = expenseFirestoreRepository.fetchNotCategorizedExpenses()
        if (result is FuncResultWithData.Success) {
            _loadingStatus.value = LoadingStatus.COMPLETED
            _searchedExpenses.value = result.data
            LogAkitaDebug("expenses:${_searchedExpenses.value}")
        } else {
            if (result is FuncResultWithData.Failure.Timeout) {
                _loadingStatus.value = LoadingStatus.TIMEOUT
            } else {
                _loadingStatus.value = LoadingStatus.ERROR
            }
        }
        return result
    }

    @Deprecated("Use searchExpenses() instead", ReplaceWith("searchExpenses(callback)"))
    fun fetchNotCategorizedExpenses(callback: (FuncStatusInfo) -> Unit) {
        viewModelScope.launch {
            val result = fetchNotCategorizedExpensesInternal()
            callback(result.toFuncStatusInfo())
        }
    }

    /** AddEditに値を渡す用 **/
    fun setToTmpExpense(expense: Expense) {
        tmpExpenseViewModel.updateTmpExpense(expense)
    }
}