package gaku.original.myapplication.data.importer

import java.time.LocalDateTime

/** Reads the expenses out of another app's export file. One implementation per app. */
interface ExpenseImporter {
    /** Saved in GeneratedType.Import, e.g. "drwallet". */
    val source: String

    /** Throws [ImportFileException] when the file is not in the expected format. */
    fun parse(bytes: ByteArray): ImportParseResult
}

/** One expense read from an export file. [rowNumber] is the row in the file, counted from 1, header included. */
data class ImportRecord(
    val rowNumber: Int,
    val datetime: LocalDateTime,
    val amount: Long,
    val categoryName: String?,
    val storeName: String?,
    val itemName: String?,
    val note: String?,
)

data class SkippedRow(
    val rowNumber: Int,
    val reason: String,
)

data class ImportParseResult(
    val records: List<ImportRecord>,
    val skipped: List<SkippedRow>,
)

class ImportFileException(message: String) : Exception(message)
