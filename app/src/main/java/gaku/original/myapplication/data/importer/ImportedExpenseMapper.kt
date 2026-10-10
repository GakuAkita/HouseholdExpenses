package gaku.original.myapplication.data.importer

import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.GeneratedType
import gaku.original.myapplication.data.repository.appTimeZone.toInstant
import java.security.MessageDigest
import java.time.ZoneId

/** Turns [ImportRecord]s into [Expense]s. Not tied to any one app's format. */
class ImportedExpenseMapper {

    /** Category names in the records that none of [categories] has. */
    fun missingCategoryNames(records: List<ImportRecord>, categories: Collection<Category>): List<String> =
        records.mapNotNull { it.categoryName }
            .distinct()
            .filter { name -> findCategory(name, categories) == null }

    /** An enabled category wins over a disabled one with the same name. */
    fun findCategory(name: String, categories: Collection<Category>): Category? =
        categories.filter { it.name.trim() == name }.maxByOrNull { it.enabled }

    /**
     * The id comes from the source and the row's contents, so importing the same file again
     * overwrites the same documents instead of adding duplicates.
     * Identical rows (two ¥150 coffees on the same day) are told apart by how many came before.
     */
    fun toExpenses(
        records: List<ImportRecord>,
        source: String,
        zoneId: ZoneId,
        categories: Collection<Category>,
        timestamp: Long,
    ): List<Expense> {
        val seen = mutableMapOf<String, Int>()
        return records.map { record ->
            val key = record.contentKey()
            val occurrence = seen.getOrDefault(key, 0)
            seen[key] = occurrence + 1
            Expense(
                id = expenseId(source, key, occurrence),
                generatedType = GeneratedType.Import(source = source),
                datetime = record.datetime.toInstant(zoneId),
                timestamp = timestamp,
                amount = record.amount,
                category = record.categoryName?.let { findCategory(it, categories) },
                note = record.note,
                storeName = record.storeName,
                itemName = record.itemName,
            )
        }
    }

    private fun ImportRecord.contentKey(): String =
        listOf(datetime.toString(), amount.toString(), categoryName, storeName, itemName, note)
            .joinToString("\u0000") { it ?: "" }

    private fun expenseId(source: String, contentKey: String, occurrence: Int): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$contentKey\u0000$occurrence".toByteArray(Charsets.UTF_8))
        return "${source}_" + digest.take(16).joinToString("") { "%02x".format(it) }
    }
}
