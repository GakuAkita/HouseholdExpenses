package gaku.original.myapplication.data.importer

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.DateTimeException
import kotlin.math.abs

/** One expense row read from a Dr.Wallet CSV export. [rowNumber] counts CSV records from 1, header included. */
data class DrWalletRecord(
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

data class DrWalletParseResult(
    val records: List<DrWalletRecord>,
    val skipped: List<SkippedRow>,
)

class DrWalletCsvException(message: String) : Exception(message)

/**
 * Columns are found by header name, so the column order does not matter and unknown columns are ignored.
 * Only the date and amount columns are required.
 */
object DrWalletCsvParser {

    private val DATE_HEADERS = listOf("日付", "利用日", "購入日", "date")
    private val TIME_HEADERS = listOf("時刻", "時間", "time")
    private val AMOUNT_HEADERS = listOf("金額", "支出金額", "合計金額", "amount")
    private val CATEGORY_HEADERS = listOf("カテゴリ", "カテゴリー", "大カテゴリ", "大項目", "category")
    private val STORE_HEADERS = listOf("店名", "店舗名", "店舗", "お店", "store")
    private val ITEM_HEADERS = listOf("品目", "商品名", "品名", "内容", "item")
    private val NOTE_HEADERS = listOf("メモ", "備考", "note", "memo")
    private val TYPE_HEADERS = listOf("収支", "収支区分", "種別", "区分", "type")

    /* The header might come after a title line or two. */
    private const val HEADER_SEARCH_ROWS = 10

    /* A date without a time is put at noon, so a small time zone difference does not move it to another day. */
    private val DEFAULT_TIME: LocalTime = LocalTime.NOON

    private val DATE_REGEX = Regex("""(\d{4})\D(\d{1,2})\D(\d{1,2})日?(?:\s+(\d{1,2}):(\d{2})(?::(\d{2}))?)?""")
    private val TIME_REGEX = Regex("""(\d{1,2}):(\d{2})(?::(\d{2}))?""")

    fun parse(text: String): DrWalletParseResult {
        val rows = CsvReader.parse(text)
        val headerIndex = rows.take(HEADER_SEARCH_ROWS).indexOfFirst { row ->
            row.findColumn(DATE_HEADERS) != null && row.findColumn(AMOUNT_HEADERS) != null
        }
        if (headerIndex < 0) {
            throw DrWalletCsvException("Could not find the header row. It needs a date (日付) and an amount (金額) column.")
        }

        val header = rows[headerIndex]
        val dateColumn = header.findColumn(DATE_HEADERS)!!
        val amountColumn = header.findColumn(AMOUNT_HEADERS)!!
        val timeColumn = header.findColumn(TIME_HEADERS)
        val categoryColumn = header.findColumn(CATEGORY_HEADERS)
        val storeColumn = header.findColumn(STORE_HEADERS)
        val itemColumn = header.findColumn(ITEM_HEADERS)
        val noteColumn = header.findColumn(NOTE_HEADERS)
        val typeColumn = header.findColumn(TYPE_HEADERS)

        val records = mutableListOf<DrWalletRecord>()
        val skipped = mutableListOf<SkippedRow>()

        for (index in headerIndex + 1 until rows.size) {
            val row = rows[index]
            val rowNumber = index + 1
            fun cell(column: Int?): String? = column?.let { row.getOrNull(it) }?.trim()?.ifEmpty { null }

            val type = cell(typeColumn)
            if (type != null && type.contains("収入")) {
                skipped.add(SkippedRow(rowNumber, "Income"))
                continue
            }
            if (type != null && (type.contains("振替") || type.contains("移動"))) {
                skipped.add(SkippedRow(rowNumber, "Transfer"))
                continue
            }

            val rawDate = cell(dateColumn)
            val datetime = rawDate?.let { parseDateTime(it, cell(timeColumn)) }
            if (datetime == null) {
                skipped.add(SkippedRow(rowNumber, "Unreadable date: ${rawDate ?: "(empty)"}"))
                continue
            }

            val rawAmount = cell(amountColumn)
            val amount = rawAmount?.let { parseAmount(it) }
            if (amount == null) {
                skipped.add(SkippedRow(rowNumber, "Unreadable amount: ${rawAmount ?: "(empty)"}"))
                continue
            }
            if (amount == 0L) {
                skipped.add(SkippedRow(rowNumber, "Amount is 0"))
                continue
            }

            records.add(
                DrWalletRecord(
                    rowNumber = rowNumber,
                    datetime = datetime,
                    amount = amount,
                    categoryName = cell(categoryColumn),
                    storeName = cell(storeColumn),
                    itemName = cell(itemColumn),
                    note = cell(noteColumn),
                )
            )
        }
        return DrWalletParseResult(records, skipped)
    }

    /** Exact header match first, so "カテゴリ" is not taken by "サブカテゴリ". */
    private fun List<String>.findColumn(candidates: List<String>): Int? {
        val normalized = map { it.trim().lowercase() }
        candidates.forEach { candidate ->
            val i = normalized.indexOf(candidate)
            if (i >= 0) return i
        }
        return null
    }

    /** Accepts 2026/09/15, 2026-9-15, 2026年9月15日, optionally followed by a time. */
    internal fun parseDateTime(date: String, time: String?): LocalDateTime? {
        val match = DATE_REGEX.matchEntire(date) ?: return null
        val (year, month, day) = match.destructured
        val localDate = try {
            LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        } catch (e: DateTimeException) {
            return null
        }
        val localTime = if (match.groupValues[4].isNotEmpty()) {
            toLocalTime(match.groupValues[4], match.groupValues[5], match.groupValues[6])
        } else {
            time?.let { TIME_REGEX.matchEntire(it) }
                ?.let { toLocalTime(it.groupValues[1], it.groupValues[2], it.groupValues[3]) }
        }
        return localDate.atTime(localTime ?: DEFAULT_TIME)
    }

    private fun toLocalTime(hour: String, minute: String, second: String): LocalTime? = try {
        LocalTime.of(hour.toInt(), minute.toInt(), second.ifEmpty { "0" }.toInt())
    } catch (e: DateTimeException) {
        null
    }

    /**
     * Accepts "1,200", "¥1,200", "1200円" and "-1200".
     * Some exports write spending as a negative number, so the sign is dropped; income is told apart by the type column.
     */
    internal fun parseAmount(raw: String): Long? {
        val cleaned = raw.replace(Regex("""[,，¥￥円\s]"""), "")
        return cleaned.toLongOrNull()?.let { abs(it) }
    }
}
