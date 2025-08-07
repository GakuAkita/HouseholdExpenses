package gaku.original.myapplication.viewModel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.repository.FirestoreRepository.ExpenseFirestoreRepository
import gaku.original.myapplication.utility.LogAkitaDebug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotCategorizedViewModel @Inject constructor(
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

    private val _notCategorizedExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val notCategorizedExpenses: StateFlow<List<Expense>> get() = _notCategorizedExpenses

    private suspend fun fetchNotCategorizedExpensesInternal(): FuncResultWithData<List<Expense>> {
        _loadingStatus.value = LoadingStatus.LOADING
        val result = expenseFirestoreRepository.fetchNotCategorizedExpenses()
        if (result is FuncResultWithData.Success) {
            _loadingStatus.value = LoadingStatus.COMPLETED
            _notCategorizedExpenses.value = result.data//成功のときだけ更新
            LogAkitaDebug("expenses:${_notCategorizedExpenses.value}")
        } else {
            if (result is FuncResultWithData.Failure.Timeout) {
                _loadingStatus.value = LoadingStatus.TIMEOUT
            } else {
                _loadingStatus.value = LoadingStatus.ERROR
            }
        }
        return result
    }

    fun fetchNotCategorizedExpenses(callback: (SuspendFuncStatusInfo) -> Unit) {
        viewModelScope.launch {
            val result = fetchNotCategorizedExpensesInternal()
            callback(result.toSuspendFuncStatusInfo())
        }
    }

    /** AddEditに値を渡す用 **/
    fun setToTmpExpense(expense: Expense) {
        tmpExpenseViewModel.updateTmpExpense(expense)
    }
}