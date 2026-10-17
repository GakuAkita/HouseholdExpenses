package gaku.original.myapplication.data.importer.drwallet

import gaku.original.myapplication.data.importer.CsvReader
import gaku.original.myapplication.data.importer.ExpenseImporter
import gaku.original.myapplication.data.importer.ImportFileException
import gaku.original.myapplication.data.importer.ImportParseResult
import gaku.original.myapplication.data.importer.ImportRecord
import gaku.original.myapplication.data.importer.SkippedRow
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Reads a CSV exported from Dr.Wallet.
 * Columns are found by header name, so the column order does not matter and unknown columns are ignored.
 * Only the date and amount columns are required.
 *
 * Dr.Wallet's export (UTF-8, no BOM) looks like this:
 * ```
 * 取引日,時分,取引金額,内容,ショップ名,電話番号,住所,カテゴリー,口座,ID,集計対象外
 * 20261011,0000,-60000,"","","","",食費,現金,aa7cb4e6...,""
 * ```
 * - 取引日 is yyyyMMdd and 時分 is HHmm, with 0000 when no time was entered.
 * - 取引金額 is signed: spending is negative, income is positive. Only negative rows are imported,
 *   since this app only keeps expenses.
 * - ID is Dr.Wallet's own id for the entry, used to keep re-imports on the same documents.
 * - 電話番号, 住所, 口座 and 集計対象外 are not used.
 *
 * Other header names (日付, 金額, ...) are accepted too, but the amount must follow the same sign rule.
 */
class DrWalletCsvImporter(
    private val csvReader: CsvReader
) : ExpenseImporter {

    override val source: String = SOURCE

    override fun parse(bytes: ByteArray): ImportParseResult {
        val rows = csvReader.parse(csvReader.decode(bytes))

        /* The header is the first row that has both a date and an amount column. */
        val headerIndex = rows.take(HEADER_SEARCH_ROWS).indexOfFirst { row ->
            row.findColumn(DATE_HEADERS) != null && row.findColumn(AMOUNT_HEADERS) != null
        }
        if (headerIndex < 0) {
            throw ImportFileException("Could not find the header row. It needs a date (日付) and an amount (金額) column.")
        }


        /* Column positions. Optional columns are null when the file doesn't have them. */
        val header = rows[headerIndex]
        val dateColumn = header.findColumn(DATE_HEADERS)!!
        val amountColumn = header.findColumn(AMOUNT_HEADERS)!!
        val timeColumn = header.findColumn(TIME_HEADERS)
        val categoryColumn = header.findColumn(CATEGORY_HEADERS)
        val storeColumn = header.findColumn(STORE_HEADERS)
        val itemColumn = header.findColumn(ITEM_HEADERS)
        val noteColumn = header.findColumn(NOTE_HEADERS)
        val idColumn = header.findColumn(ID_HEADERS)

        val records = mutableListOf<ImportRecord>()
        val skipped = mutableListOf<SkippedRow>()

        /* Each row either becomes a record or is skipped with a reason that the import screen shows. */
        for (index in headerIndex + 1 until rows.size) {
            val row = rows[index]
            /* Counted from 1 like a spreadsheet, so the user can find the row in the file. */
            val rowNumber = index + 1

            /* A cell's trimmed text, or null when the column is missing, the row is short or the cell is blank. */
            fun cell(column: Int?): String? =
                column?.let { row.getOrNull(it) }?.trim()?.ifEmpty { null }

            val rawDate = cell(dateColumn)
            val datetime = rawDate?.let { parseDateTime(it, cell(timeColumn)) }
            if (datetime == null) {
                skipped.add(SkippedRow(rowNumber, "Unreadable date: ${rawDate ?: "(empty)"}"))
                continue
            }

            /*
             * Dr.Wallet writes spending as a negative amount and income as a positive one.
             * This app only keeps expenses, so only negative amounts are imported.
             * The sign is read after ¥, 円 and commas are removed, so "¥-1,200" counts as negative.
             */
            val rawAmount = cell(amountColumn)
            val signedAmount = rawAmount?.let { parseSignedAmount(it) }
            if (signedAmount == null) {
                skipped.add(SkippedRow(rowNumber, "Unreadable amount: ${rawAmount ?: "(empty)"}"))
                continue
            }
            if (signedAmount == 0L) {
                skipped.add(SkippedRow(rowNumber, "Amount is 0"))
                continue
            }
            if (signedAmount > 0) {
                skipped.add(SkippedRow(rowNumber, "Income"))
                continue
            }
            /* Expenses are stored as positive amounts. */
            val amount = -signedAmount

            records.add(
                ImportRecord(
                    rowNumber = rowNumber,
                    datetime = datetime,
                    amount = amount,
                    categoryName = cell(categoryColumn),
                    storeName = cell(storeColumn),
                    itemName = cell(itemColumn),
                    note = cell(noteColumn),
                    sourceId = cell(idColumn),
                )
            )
        }
        return ImportParseResult(records, skipped)
    }

    /**
     * Index of the first header in [candidates] that this row has, trying them in order.
     * Exact header match first, so "カテゴリ" is not taken by "サブカテゴリ".
     * Headers are compared trimmed and lowercased, so "ID", "Date" and " 金額 " all match.
     */
    private fun List<String>.findColumn(candidates: List<String>): Int? {
        val normalized = map { it.trim().lowercase() }
        candidates.forEach { candidate ->
            val i = normalized.indexOf(candidate)
            if (i >= 0) return i
        }
        return null
    }

    /**
     * Accepts 2026/09/15, 2026-9-15, 2026年9月15日, 20260915, optionally followed by a time.
     * A time inside [date] wins over [time], the separate time column (08:30 or 0830).
     * Returns null for a date that doesn't exist, such as 2026/02/30.
     */
    internal fun parseDateTime(date: String, time: String?): LocalDateTime? {
        val match = DATE_REGEX.matchEntire(date)
        val (year, month, day) = match?.destructured
            ?: COMPACT_DATE_REGEX.matchEntire(date)?.destructured
            ?: return null
        val localDate = try {
            LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        } catch (e: DateTimeException) {
            return null
        }
        val localTime = if (match != null && match.groupValues[4].isNotEmpty()) {
            toLocalTime(match.groupValues[4], match.groupValues[5], match.groupValues[6])
        } else {
            /* Dr.Wallet writes 0000 when no time was entered. */
            time?.takeIf { it != NO_TIME }
                ?.let { TIME_REGEX.matchEntire(it) }
                ?.let { toLocalTime(it.groupValues[1], it.groupValues[2], it.groupValues[3]) }
        }
        return localDate.atTime(localTime ?: DEFAULT_TIME)
    }

    /** Null for a time that doesn't exist, such as 25:00. A missing second is 0. */
    private fun toLocalTime(hour: String, minute: String, second: String): LocalTime? = try {
        LocalTime.of(hour.toInt(), minute.toInt(), second.ifEmpty { "0" }.toInt())
    } catch (e: DateTimeException) {
        null
    }

    /**
     * Reads an amount cell as a signed number: "-1,200", "¥-1,200" and "-1200円" are all -1200.
     * This only reads the number. [parse] then imports negative amounts and skips positive ones
     * like "1200" as income. Decimals like "12.5" can't be read and give null.
     */
    internal fun parseSignedAmount(raw: String): Long? =
        raw.replace(Regex("""[,，¥￥円\s]"""), "").toLongOrNull()

    companion object {
        /* Saved in GeneratedType.Import and used as the expense id prefix. Changing it changes every id. */
        const val SOURCE = "drwallet"

        /*
         * Header names each column may have, most likely first. All lowercase, see findColumn.
         * The first names are a generic CSV's; Dr.Wallet's own (取引日, 時分, 取引金額, ショップ名) come later.
         */

        private val DATE_HEADERS = listOf("日付", "利用日", "購入日", "date", "取引日")
        private val TIME_HEADERS = listOf("時刻", "時間", "time", "時分")
        private val AMOUNT_HEADERS = listOf("金額", "支出金額", "合計金額", "amount", "取引金額")
        private val CATEGORY_HEADERS =
            listOf("カテゴリ", "カテゴリー", "大カテゴリ", "大項目", "category")
        private val STORE_HEADERS = listOf("店名", "店舗名", "店舗", "お店", "store", "ショップ名")
        private val ITEM_HEADERS = listOf("品目", "商品名", "品名", "内容", "item")
        private val NOTE_HEADERS = listOf("メモ", "備考", "note", "memo")
        private val ID_HEADERS = listOf("id")

        /* The header might come after a title line or two. */
        private const val HEADER_SEARCH_ROWS = 10

        /* A date without a time is put at noon, so a small time zone difference does not move it to another day. */
        private val DEFAULT_TIME: LocalTime = LocalTime.NOON

        /*
         * Groups: 1 year, 2 month, 3 day, then an optional time: 4 hour, 5 minute, 6 second.
         * Any one non-digit separates the date parts, so / - . and 年 月 all work.
         */
        private val DATE_REGEX =
            Regex("""(\d{4})\D(\d{1,2})\D(\d{1,2})日?(?:\s+(\d{1,2}):(\d{2})(?::(\d{2}))?)?""")

        /* yyyyMMdd, as in Dr.Wallet's 取引日. Groups: 1 year, 2 month, 3 day. */
        private val COMPACT_DATE_REGEX = Regex("""(\d{4})(\d{2})(\d{2})""")

        /* 8:30, 08:30:15, 0830 or 083015. Groups: 1 hour, 2 minute, 3 second (may be empty). */
        private val TIME_REGEX = Regex("""(\d{1,2}):?(\d{2})(?::?(\d{2}))?""")

        /* What Dr.Wallet writes in 時分 when no time was entered; read as no time, not midnight. */
        private const val NO_TIME = "0000"
    }
}
