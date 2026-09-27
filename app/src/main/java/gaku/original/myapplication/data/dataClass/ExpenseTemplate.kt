package gaku.original.myapplication.data.dataClass

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/**
 * The expense part of a RepeatAdd.
 * id, datetime, timestamp and generatedType are decided when an expense is actually added.
 */
@Serializable
data class ExpenseTemplate(
    val amount: Long,
    val category: Category,
    val note: String? = null,
    val storeName: String? = null,
    val itemName: String? = null,
) {
    /** Creates a new expense from this template, with a new id. */
    fun toExpense(datetime: Instant, generatedType: GeneratedType): Expense = Expense(
        id = UUID.randomUUID().toString(),
        datetime = datetime,
        timestamp = System.currentTimeMillis(),
        generatedType = generatedType,
        amount = amount,
        category = category,
        note = note,
        storeName = storeName,
        itemName = itemName,
    )
}
