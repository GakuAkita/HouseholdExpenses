package gaku.original.myapplication.data.repository.expense

import com.google.firebase.firestore.FirebaseFirestore
import gaku.original.myapplication.data.dataClass.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class ExpenseRepositoryFirestore: ExpenseRepository {

    private val _expenses = MutableStateFlow<Map<String, Expense>>(emptyMap())
    override val expenses: StateFlow<Map<String, Expense>>
        get() = _expenses

    override fun startListening() {
        TODO("Not yet implemented")
    }

    override fun stopListening() {
        TODO("Not yet implemented")
    }

    override suspend fun addExpense(expense: Expense): Expense {
        val newExpense = expense.copy(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis()
        )
        _expenses.value += (newExpense.id!! to newExpense)
        return newExpense
    }

    override suspend fun updateExpense(expense: Expense): Expense {
        _expenses.value = _expenses.value + (expense.id!! to expense)
        return expense
    }

    override suspend fun removeExpense(expense: Expense) {
        _expenses.value = _expenses.value - expense.id!!
    }
}