package gaku.original.myapplication.data.repository.expense

import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.GeneratedType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.time.Instant

class FakeExpenseRepository : ExpenseRepository {
    private val sampleExpenses = mapOf(
        "1" to sampleExpense(id = "1", amount = 1000),
        "2" to sampleExpense(id = "2", amount = 2000000)
    )

    private val _expenses = MutableStateFlow<Map<String, Map<String, Expense>>>(emptyMap())
    override val expenses: StateFlow<Map<String, Map<String, Expense>>>
        get() = _expenses

    init {
        Timber.d("Created. ${hashCode()}")
    }

    override fun startListening(subscriptionId: String, query: ExpenseQuery) {
        // クエリに基づいたフィルタリングは一旦省略し、サンプルデータをそのまま入れる
        _expenses.value = _expenses.value + (subscriptionId to sampleExpenses)
    }

    override fun stopListening(subscriptionId: String) {
        _expenses.value = _expenses.value - subscriptionId
    }

    override suspend fun addExpense(expense: Expense): Expense {
        // 全ての購読に対して反映させる（簡易的な実装）
        _expenses.value = _expenses.value.mapValues { it.value + (expense.id to expense) }
        return expense
    }

    override suspend fun updateExpense(expense: Expense): Expense {
        _expenses.value = _expenses.value.mapValues { it.value + (expense.id to expense) }
        return expense
    }

    override suspend fun removeExpense(id: String) {
        _expenses.value = _expenses.value.mapValues { it.value - id }
    }
}

private fun sampleExpense(id: String, amount: Long) = Expense(
    id = id,
    generatedType = GeneratedType.Manual,
    datetime = Instant.now(),
    timestamp = System.currentTimeMillis(),
    amount = amount,
    category = null,
    note = null,
    storeName = null,
    itemName = null,
)
