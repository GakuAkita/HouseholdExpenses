package gaku.original.myapplication.data.repository.expense

import gaku.original.myapplication.data.dataClass.Expense
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant

data class ExpenseQuery(
    val datetimeFromOrEqual: Instant? = null,/* includes start time */
    val datetimeTo: Instant? = null,/* endtime not included */
)

interface ExpenseRepository {
    fun startListening(subscriptionId: String, query: ExpenseQuery)

    fun stopListening(subscriptionId: String)

    val expenses: StateFlow<Map<String, Map<String, Expense>>>

    suspend fun addExpense(expense: Expense): Expense

    suspend fun updateExpense(expense: Expense): Expense

    suspend fun removeExpense(id: String)
}