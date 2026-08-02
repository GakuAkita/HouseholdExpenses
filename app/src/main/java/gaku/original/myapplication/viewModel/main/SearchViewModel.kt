package gaku.original.myapplication.viewModel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.ExpenseSearchFilter
import android.util.Log
import gaku.original.myapplication.data.dataClass.getDefaultSearchFilter
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import gaku.original.myapplication.useCase.SearchFilterUseCase
import gaku.original.myapplication.viewModel.shared.ExpenseSharedViewModel
import gaku.original.myapplication.viewModel.shared.TemporaryExpenseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val expenseFirestoreRepository: ExpenseRepository,
    private val tmpExpenseViewModel: TemporaryExpenseViewModel,
    private val expenseSharedViewModel: ExpenseSharedViewModel,
    private val searchFilterUseCase: SearchFilterUseCase,
) : ViewModel() {
    override fun onCleared() {
        /**
         * 他のボトムバーに移動したときにViewModelは破棄される
         */
        super.onCleared()
        Log.d("SearchViewModel", "onCleared: ViewModel cleared")
    }

    private val _loadingStatus = MutableStateFlow(LoadingStatus.SUCCESS)
    val loadingStatus: StateFlow<LoadingStatus> get() = _loadingStatus

    private val _searchedExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val searchedExpenses: StateFlow<List<Expense>> get() = _searchedExpenses

    // 現在のフィルター条件（初期化時にSharedPreferencesから復元）
//    private val _currentFilter = MutableStateFlow(loadSavedFilter())
//    val currentFilter: StateFlow<ExpenseSearchFilter> get() = _currentFilter

    // カテゴリー一覧（ExpenseSharedViewModelから取得）
//    val allCategories: StateFlow<List<Category>> get() = expenseSharedViewModel.allCategories

    /**
     * 保存されたフィルターを復元
     */
//    private fun loadSavedFilter(): ExpenseSearchFilter {
//        val result = searchFilterUseCase.loadSavedFilterOrDefault()
//        return when (result) {
//            is FuncResultWithData.Success -> {
//                Log.d("SearchViewModel", "loadSavedFilter: Loaded filter successfully: ${result.data}")
//                result.data
//            }
//            is FuncResultWithData.Failure -> {
//                Log.e("SearchViewModel", "loadSavedFilter: Failed to load filter: ${result.errorMessage}, using default")
//                getDefaultSearchFilter()
//            }
//            else -> {
//                Log.e("SearchViewModel", "loadSavedFilter: Unexpected result type: $result, using default")
//                getDefaultSearchFilter()
//            }
//        }
//    }
//
//    /**
//     * フィルター条件を更新し、SharedPreferencesに保存
//     */
//    fun updateFilter(filter: ExpenseSearchFilter) {
//        _currentFilter.value = filter
//        val result = searchFilterUseCase.saveSearchFilter(filter)
//        when (result) {
//            is FuncResultWithData.Success -> {
//                Log.d("SearchViewModel", "updateFilter: Filter saved successfully: ${result.data}")
//            }
//            is FuncResultWithData.Failure -> {
//                Log.e("SearchViewModel", "updateFilter: Failed to save filter: ${result.errorMessage}")
//            }
//            else -> {
//                Log.e("SearchViewModel", "updateFilter: Unexpected result type: $result")
//            }
//        }
//    }
//
//    /**
//     * フィルター条件をリセット（カテゴリーがnullのデフォルト）し、SharedPreferencesに保存
//     */
//    fun resetFilter() {
//        val defaultFilter = getDefaultSearchFilter()
//        _currentFilter.value = defaultFilter
//        val result = searchFilterUseCase.saveSearchFilter(defaultFilter)
//        when (result) {
//            is FuncResultWithData.Success -> {
//                Log.d("SearchViewModel", "resetFilter: Default filter saved successfully: ${result.data}")
//            }
//            is FuncResultWithData.Failure -> {
//                Log.e("SearchViewModel", "resetFilter: Failed to save default filter: ${result.errorMessage}")
//            }
//            else -> {
//                Log.e("SearchViewModel", "resetFilter: Unexpected result type: $result")
//            }
//        }
//    }
//
//    /**
//     * 現在のフィルター条件で検索を実行
//     * コスト最適化のため、テキスト検索が含まれる場合は厳格な検索を使用
//     */
//    private suspend fun searchExpensesInternal(): FuncResultWithData<List<Expense>> {
//        _loadingStatus.value = LoadingStatus.LOADING
//
//        // テキスト検索が含まれている場合は厳格な検索を使用（コスト削減）
//        val hasTextSearch = _currentFilter.value.storeName != null ||
//                           _currentFilter.value.itemName != null ||
//                           _currentFilter.value.note != null
//
//        val result = if (hasTextSearch) {
//            expenseFirestoreRepository.searchExpensesStrict(_currentFilter.value)
//        } else {
//            expenseFirestoreRepository.searchExpenses(_currentFilter.value)
//        }
//
//        if (result is FuncResultWithData.Success) {
//            _loadingStatus.value = LoadingStatus.SUCCESS
//            _searchedExpenses.value = result.data
//            Log.d("SearchViewModel", "searchExpensesInternal: Found ${result.data.size} expenses")
//        } else {
//            if (result is FuncResultWithData.Failure.Timeout) {
//                _loadingStatus.value = LoadingStatus.TIMEOUT
//            } else {
//                _loadingStatus.value = LoadingStatus.ERROR
//            }
//        }
//        return result
//    }
//
//    /**
//     * 検索を実行（公開API）
//     */
//    fun searchExpenses(callback: (FuncStatusInfo) -> Unit) {
//        viewModelScope.launch {
//            val result = searchExpensesInternal()
//            callback(result.toFuncStatusInfo())
//        }
//    }
//
//    /**
//     * 特定のフィルターで検索を実行
//     */
//    fun searchWithFilter(filter: ExpenseSearchFilter, callback: (FuncStatusInfo) -> Unit) {
//        updateFilter(filter)
//        searchExpenses(callback)
//    }
//
//    // 後方互換性のため残す（将来的に削除予定）
//    @Deprecated("Use searchExpenses() instead", ReplaceWith("searchExpenses(callback)"))
//    private suspend fun fetchNotCategorizedExpensesInternal(): FuncResultWithData<List<Expense>> {
//        _loadingStatus.value = LoadingStatus.LOADING
//        val result = expenseFirestoreRepository.fetchNotCategorizedExpenses()
//        if (result is FuncResultWithData.Success) {
//            _loadingStatus.value = LoadingStatus.SUCCESS
//            _searchedExpenses.value = result.data
//            Log.d("SearchViewModel", "searchExpensesInternal: Found ${result.data.size} expenses")
//        } else {
//            if (result is FuncResultWithData.Failure.Timeout) {
//                _loadingStatus.value = LoadingStatus.TIMEOUT
//            } else {
//                _loadingStatus.value = LoadingStatus.ERROR
//            }
//        }
//        return result
//    }
//
//    @Deprecated("Use searchExpenses() instead", ReplaceWith("searchExpenses(callback)"))
//    fun fetchNotCategorizedExpenses(callback: (FuncStatusInfo) -> Unit) {
//        viewModelScope.launch {
//            val result = fetchNotCategorizedExpensesInternal()
//            callback(result.toFuncStatusInfo())
//        }
//    }
//
//    /** AddEditに値を渡す用 **/
//    fun setToTmpExpense(expense: Expense) {
//        tmpExpenseViewModel.updateTmpExpense(expense)
//    }
}