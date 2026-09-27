package gaku.original.myapplication.data.repository.expense

import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.toGeneratedType
import gaku.original.myapplication.data.repository.category.CategoryFirestore
import gaku.original.myapplication.data.repository.category.toFirestore

/**
 * The shape of an expense document in Firestore. Only the repositories use this class.
 *
 * Properties are nullable vars with defaults so that toObject() can create it.
 * Keep the property names in sync with Firestore functions and rules.
 */
data class ExpenseFirestore(
    var id: String? = null,
    var timestamp: Long? = null,
    var datetime: String? = null,
    var amount: Long? = null,
    var category: CategoryFirestore? = null,
    var note: String? = null,
    var storeName: String? = null,
    var itemName: String? = null,
    /* GeneratedType.toSerialized(), e.g. "repeat_add___<id>" */
    var generatedType: String? = null,
) {
    fun toDomain(): Expense = Expense(
        id = id ?: error("id is null"),
        timestamp = timestamp,
        datetime = datetime ?: error("datetime is null"),
        amount = amount,
        category = category?.toDomain(),
        note = note,
        storeName = storeName,
        itemName = itemName,
        generatedType = generatedType?.toGeneratedType()
    )

    /**
     * The expense of RepeatAdd is a template. id, datetime, timestamp and generatedType are decided
     * when the expense is actually added, so they are not restored.
     */
    fun toDomainForRepeatAdd(): Expense = Expense(
        category = category?.toDomain() ?: error("category is null"),
        amount = amount ?: error("amount is null"),
        storeName = storeName,
        itemName = itemName,
        note = note,
    )
}

fun Expense.toFirestore(): ExpenseFirestore = ExpenseFirestore(
    id = id,
    timestamp = timestamp,
    datetime = datetime,
    amount = amount,
    category = category?.toFirestore(),
    note = note,
    storeName = storeName,
    itemName = itemName,
    generatedType = generatedType?.toSerialized()
)
