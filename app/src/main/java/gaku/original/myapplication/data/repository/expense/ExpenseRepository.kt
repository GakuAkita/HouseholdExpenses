package gaku.original.myapplication.data.repository.expense

import gaku.original.myapplication.data.dataClass.Expense
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant

data class ExpenseQuery(
    val from: Instant,
    val to:Instant
)

interface ExpenseRepository {
    fun startListening()

    fun stopListening()

    val expenses: StateFlow<Map<String, Expense>>

    suspend fun addExpense(expense: Expense): Expense

    suspend fun updateExpense(expense: Expense): Expense

    suspend fun removeExpense(expense: Expense)
}