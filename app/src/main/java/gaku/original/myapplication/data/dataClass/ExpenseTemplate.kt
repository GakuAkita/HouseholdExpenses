package gaku.original.myapplication.data.dataClass

import kotlinx.serialization.Serializable
import java.time.Instant

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
    fun toExpense(datetime: Instant, generatedType: GeneratedType): Expense = Expense(
        datetime = datetime.toString(),
        generatedType = generatedType,
        amount = amount,
        category = category,
        note = note,
        storeName = storeName,
        itemName = itemName,
    )
}
