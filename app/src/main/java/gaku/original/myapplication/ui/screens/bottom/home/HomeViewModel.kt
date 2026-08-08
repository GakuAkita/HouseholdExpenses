package gaku.original.myapplication.ui.screens.bottom.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.repository.expense.ExpenseQuery
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.YearMonth

data class HomeUiState(
    val isLoading:Boolean = false,
    val message:String? = null,
    val selectedMonth: YearMonth = YearMonth.now(),
    val shownExpenses:List<Expense> = emptyList()
)
class HomeViewModel(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val expenseRepository = app.appContainer.sessionContainer!!.expenseRepository
                HomeViewModel(expenseRepository)
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")

        expenseRepository.startListening(ExpenseQuery())
        viewModelScope.launch {
            expenseRepository.expenses.collect{ expenses ->
                _uiState.update {
                    it.copy(
                        shownExpenses = expenses.values.toList()
                    )
                }
            }
        }
    }

    fun onMonthChanged(month: YearMonth){
        Timber.d("Swiped to ${month.year}-${month.monthValue} hash=${hashCode()}");
        _uiState.update {
            it.copy(
                selectedMonth = month,
            )
        }
    }

    override fun onCleared() {
        Timber.d("onCleared called. ${hashCode()}")
        expenseRepository.stopListening()
        super.onCleared()
    }
}