package gaku.original.myapplication.data.importer

import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.GeneratedType
import gaku.original.myapplication.data.repository.appTimeZone.toInstant
import java.security.MessageDigest
import java.time.ZoneId

/**
 * Turns [ImportRecord]s into [Expense]s. Not tied to any one app's format.
 * An [ExpenseImporter] reads the file into records first; this class decides ids and categories.
 */
class ImportedExpenseMapper {

    /**
     * Category names in the records that none of [categories] has, each once, in file order.
     * The import screen lists these and can create them before importing.
     */
    fun missingCategoryNames(records: List<ImportRecord>, categories: Collection<Category>): List<String> =
        records.mapNotNull { it.categoryName }
            .distinct()
            .filter { name -> findCategory(name, categories) == null }

    /**
     * The category whose name is [name], or null when there is none.
     * The app's category names are trimmed before comparing; [name] is already trimmed by the importer.
     * An enabled category wins over a disabled one with the same name.
     */
    fun findCategory(name: String, categories: Collection<Category>): Category? =
        categories.filter { it.name.trim() == name }.maxByOrNull { it.enabled }

    /**
     * The id comes from the source and the other app's own id, or the row's contents when there is none,
     * so importing the same file again overwrites the same documents instead of adding duplicates.
     * The other app's id also keeps an entry edited there on the same document, and keeps ids stable
     * when a parser change starts reading more of a row.
     * Identical rows (two ¥150 coffees on the same day) are told apart by how many came before.
     *
     * @param source the importer's [ExpenseImporter.source], e.g. "drwallet". It prefixes every id.
     * @param zoneId the app's time zone. The file's dates have no zone, so they are read in this one.
     * @param categories the app's categories. A record whose category isn't found gets no category.
     * @param timestamp when the import ran, saved on every expense.
     */
    fun toExpenses(
        records: List<ImportRecord>,
        source: String,
        zoneId: ZoneId,
        categories: Collection<Category>,
        timestamp: Long,
    ): List<Expense> {
        /* How many records with each key came before, so identical rows still get different ids. */
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

    /**
     * What identifies a record: the other app's id when it has one, otherwise everything read from the row.
     * Fields are joined with \u0000, which never appears in a CSV cell, so ("ab", "c") and ("a", "bc") differ.
     * The "id" prefix keeps an id-based key from ever equalling a contents-based one.
     * Changing what goes into the key changes ids, so files imported before would be added again.
     */
    private fun ImportRecord.contentKey(): String =
        sourceId?.let { "id\u0000$it" } ?: listOf(datetime.toString(), amount.toString(), categoryName, storeName, itemName, note)
            .joinToString("\u0000") { it ?: "" }

    /**
     * "<source>_" plus the first 16 bytes of SHA-256(key, occurrence) as 32 hex characters.
     * The key is hashed because it may hold characters a document id can't, such as "/".
     */
    private fun expenseId(source: String, contentKey: String, occurrence: Int): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$contentKey\u0000$occurrence".toByteArray(Charsets.UTF_8))
        return "${source}_" + digest.take(16).joinToString("") { "%02x".format(it) }
    }
}
