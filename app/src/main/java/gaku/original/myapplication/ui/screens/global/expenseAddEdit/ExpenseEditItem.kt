package gaku.original.myapplication.ui.screens.global.expenseAddEdit

import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.GeneratedType
import java.time.Instant
import java.util.UUID

/**
 * One expense on the add/edit screen.
 * The screen always holds a list of these, even when adding a single expense.
 */
data class ExpenseEditItem(
    /* The stored expense this item edits. null when this item is a new expense. */
    val original: Expense? = null,
    val amount: Long? = null,
    val category: Category? = null,
    val note: String? = null,
    val productName: String? = null,
) {
    /* datetime, storeName and generatedType are common to all the items on the screen. */
    fun toExpense(
        datetime: Instant,
        storeName: String,
        generatedType: GeneratedType,
        now: Long,
    ): Expense = Expense(
        id = original?.id ?: UUID.randomUUID().toString(),
        generatedType = generatedType,
        datetime = datetime,
        timestamp = original?.timestamp ?: now,
        amount = amount!!,
        category = category,
        note = note,
        storeName = storeName,
        itemName = productName,
    )
}

fun List<ExpenseEditItem>.updateAt(
    index: Int,
    transform: (ExpenseEditItem) -> ExpenseEditItem
): List<ExpenseEditItem> = mapIndexed { i, item -> if (i == index) transform(item) else item }

/* In split input, the last item's amount is the remaining of the total amount. */
fun List<ExpenseEditItem>.withRemainingOnLast(totalAmount: Long): List<ExpenseEditItem> {
    val remaining = (totalAmount - dropLast(1).sumOf { it.amount ?: 0L }).coerceAtLeast(0L)
    return updateAt(lastIndex) { it.copy(amount = remaining) }
}
