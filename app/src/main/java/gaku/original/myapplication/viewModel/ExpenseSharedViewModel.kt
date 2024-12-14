package gaku.original.myapplication.viewModel

import DbListenerManager
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExpenseSharedViewModel(
    private val expenseRepository: ExpenseRepository,
    private val dbListenerManager: DbListenerManager
):ViewModel() {

    private val _allExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val allExpense: StateFlow<List<Expense>> get() = _allExpenses

    //realtimeDbReferenceからとっても良いが、引数が増えるのでdbListenerManagerから取る
    private val expenseRef: DatabaseReference
        get() = dbListenerManager.expenseRef

    private val
}