package gaku.original.myapplication.viewModel.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.Repository.FirestoreRepository.CategoryFirestoreRepository
import gaku.original.myapplication.data.Repository.FirestoreRepository.ExpenseFirestoreRepository
import gaku.original.myapplication.data.dataClass.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class NotCategorizedViewModel @Inject constructor(
    private val expenseFirestoreRepository: ExpenseFirestoreRepository,
    private val categoryFirestoreRepository: CategoryFirestoreRepository,
) : ViewModel() {
    private val _loadingStatus = MutableStateFlow(LoadingStatus.COMPLETED)
    val loadingStatus: StateFlow<LoadingStatus> get() = _loadingStatus

    private val _notCategorizedExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val notCategorizedExpenses: StateFlow<List<Expense>> get() = _notCategorizedExpenses

    private suspend fun fetchNotCategorizedExpenses() {
        _loadingStatus.value = LoadingStatus.LOADING
        val result = expenseFirestoreRepository.fetchNotCategorizedExpenses()
        if (result is FetchResult.Success) {
            _loadingStatus.value = LoadingStatus.COMPLETED
            _notCategorizedExpenses.value = result.data//成功のときだけ更新
        } else {
            if (result is FetchResult.Failure.Timeout) {
                _loadingStatus.value = LoadingStatus.TIMEOUT
            } else {
                _loadingStatus.value = LoadingStatus.ERROR
            }
        }
    }
}