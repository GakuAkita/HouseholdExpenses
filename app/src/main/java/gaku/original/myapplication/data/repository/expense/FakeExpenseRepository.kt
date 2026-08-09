package gaku.original.myapplication.data.repository.expense

import com.google.type.DateTime
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.repository.appTimeZone.toIsoUtcString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class FakeExpenseRepository : ExpenseRepository {
    private val sampleExpenses = mapOf(
        "1" to Expense(
            id = "1",
            amount = 1000,
            datetime = LocalDateTime.now().toIsoUtcString(ZoneId.of("Asia/Tokyo"))
        ),
        "2" to Expense(
            id = "2",
            amount = 2000,
            datetime = LocalDateTime.now().toIsoUtcString(ZoneId.of("Asia/Tokyo"))
        )
    )

    private val _expenses = MutableStateFlow<Map<String, Expense>>(emptyMap())
    override val expenses: StateFlow<Map<String, Expense>>
        get() = _expenses

    init {
        Timber.d("Created. ${hashCode()}")
    }

    override fun startListening(query: ExpenseQuery) {
        _expenses.value = sampleExpenses
        return
    }

    override fun stopListening() {
        _expenses.value = emptyMap()
        return
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
        _expenses.value += (expense.id!! to expense)
        return expense
    }

    override suspend fun removeExpense(expense: Expense) {
        _expenses.value -= expense.id!!
    }
}