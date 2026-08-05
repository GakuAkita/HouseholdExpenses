package gaku.original.myapplication.ui.screens.bottom.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import timber.log.Timber

class HomeViewModel(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

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