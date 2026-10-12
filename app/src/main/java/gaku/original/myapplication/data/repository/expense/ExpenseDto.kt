package gaku.original.myapplication.data.repository.expense

import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.ExpenseTemplate
import gaku.original.myapplication.data.dataClass.toGeneratedType
import gaku.original.myapplication.data.repository.category.CategoryDto
import gaku.original.myapplication.data.repository.category.toDto
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * The shape of an expense as stored in a database. Only the repositories use this class.
 *
 * Properties are nullable vars with defaults so that Firestore toObject() or Realtime DB getValue() can create it.
 * Keep the property names in sync with Firestore functions and rules.
 */
data class ExpenseDto(
    var id: String? = null,
    var timestamp: Long? = null,
    var datetime: String? = null,
    var amount: Long? = null,
    var category: CategoryDto? = null,
    var note: String? = null,
    var storeName: String? = null,
    var itemName: String? = null,
    /* GeneratedType.toSerialized(), e.g. "repeat_add___<id>" */
    var generatedType: String? = null,
) {
    fun toDomain(): Expense = Expense(
        id = id ?: error("id is null"),
        timestamp = timestamp ?: error("timestamp is null"),
        datetime = Instant.parse(datetime ?: error("datetime is null")),
        amount = amount ?: error("amount is null"),
        category = category?.toDomain(),
        note = note,
        storeName = storeName,
        itemName = itemName,
        generatedType = generatedType?.toGeneratedType() ?: error("generatedType is null")
    )

    /** RepeatAdd stores its expense template in the same shape, without id, datetime, timestamp and generatedType. */
    fun toExpenseTemplate(): ExpenseTemplate = ExpenseTemplate(
        amount = amount ?: error("amount is null"),
        category = category?.toDomain() ?: error("category is null"),
        note = note,
        storeName = storeName,
        itemName = itemName,
    )
}

fun ExpenseTemplate.toDto(): ExpenseDto = ExpenseDto(
    amount = amount,
    category = category.toDto(),
    note = note,
    storeName = storeName,
    itemName = itemName,
)

fun Expense.toDto(): ExpenseDto = ExpenseDto(
    id = id,
    timestamp = timestamp,
    datetime = datetime.toDatetimeString(),
    amount = amount,
    category = category?.toDto(),
    note = note,
    storeName = storeName,
    itemName = itemName,
    generatedType = generatedType.toSerialized()
)

/* Same format as JavaScript toISOString(), which Cloud Functions use, e.g. "2026-09-30T15:00:00.000Z". */
private val DATETIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC)

/**
 * The string stored in ExpenseDto.datetime. Queries compare datetime as a string, so every writer must use this
 * fixed format. Instant.toString() leaves out zero milliseconds, and "...00Z" sorts after "...00.000Z".
 * Sub-millisecond precision is dropped.
 */
fun Instant.toDatetimeString(): String = DATETIME_FORMATTER.format(this)
