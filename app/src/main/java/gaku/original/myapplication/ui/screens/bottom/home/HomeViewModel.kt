package gaku.original.myapplication.ui.screens.bottom.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

data class HomeUiState(
     val isLoading:Boolean = false,
    val message:String? = null
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

    }

    override fun onCleared() {
        Timber.d("onCleared called. ${hashCode()}")
        super.onCleared()
    }
}