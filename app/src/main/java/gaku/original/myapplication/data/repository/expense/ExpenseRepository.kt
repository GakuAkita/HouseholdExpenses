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

    /** Saves a new expense. The caller decides the id, so saving again overwrites the same document. */
    suspend fun addExpense(expense: Expense): Expense

    /**
     * Saves many expenses at once, overwriting any with the same id.
     * [onProgress] gets the number saved so far. Not atomic: if it fails part way, the earlier ones stay saved.
     */
    suspend fun addExpenses(expenses: List<Expense>, onProgress: (Int) -> Unit = {})

    suspend fun updateExpense(expense: Expense): Expense

    suspend fun removeExpense(id: String)
}