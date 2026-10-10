package gaku.original.myapplication.data.importer.drwallet

import gaku.original.myapplication.data.importer.CsvReader
import gaku.original.myapplication.data.importer.ImportFileException
import gaku.original.myapplication.data.importer.ImportRecord
import gaku.original.myapplication.data.importer.SkippedRow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class DrWalletCsvImporterTest {

    private val importer = DrWalletCsvImporter(CsvReader())

    private fun parse(csv: String) = importer.parse(csv.toByteArray(Charsets.UTF_8))

    @Test
    fun parse_readsColumnsByHeaderName() {
        val csv = """
            日付,カテゴリ,サブカテゴリ,店名,品目,金額,メモ
            2026/09/15,食費,野菜,スーパー,キャベツ,"1,200",特売
        """.trimIndent()

        val result = parse(csv)

        assertEquals(
            listOf(
                ImportRecord(
                    rowNumber = 2,
                    datetime = LocalDateTime.of(2026, 9, 15, 12, 0),
                    amount = 1200,
                    categoryName = "食費",
                    storeName = "スーパー",
                    itemName = "キャベツ",
                    note = "特売",
                )
            ),
            result.records
        )
        assertEquals(emptyList<SkippedRow>(), result.skipped)
    }

    @Test
    fun parse_findsHeaderAfterTitleLine() {
        val csv = "Dr.Wallet export\n金額,日付\n500,2026-09-15\n"

        val result = parse(csv)

        assertEquals(1, result.records.size)
        assertEquals(3, result.records[0].rowNumber)
        assertNull(result.records[0].categoryName)
    }

    @Test
    fun parse_usesTimeColumn() {
        val csv = "日付,時刻,金額\n2026/09/15,08:30,300\n"

        assertEquals(LocalDateTime.of(2026, 9, 15, 8, 30), parse(csv).records[0].datetime)
    }

    @Test
    fun parse_skipsIncomeTransferAndBadRows() {
        val csv = """
            日付,収支,金額
            2026/09/15,支出,100
            2026/09/15,収入,200000
            2026/09/15,振替,3000
            not a date,支出,100
            2026/09/16,支出,abc
            2026/09/17,支出,0
        """.trimIndent()

        val result = parse(csv)

        assertEquals(listOf(100L), result.records.map { it.amount })
        assertEquals(
            listOf(
                SkippedRow(3, "Income"),
                SkippedRow(4, "Transfer"),
                SkippedRow(5, "Unreadable date: not a date"),
                SkippedRow(6, "Unreadable amount: abc"),
                SkippedRow(7, "Amount is 0"),
            ),
            result.skipped
        )
    }

    @Test(expected = ImportFileException::class)
    fun parse_throwsWithoutHeader() {
        parse("a,b\n1,2\n")
    }

    @Test
    fun parseDateTime_formats() {
        assertEquals(LocalDateTime.of(2026, 9, 5, 12, 0), importer.parseDateTime("2026/9/5", null))
        assertEquals(LocalDateTime.of(2026, 9, 5, 12, 0), importer.parseDateTime("2026年9月5日", null))
        assertEquals(LocalDateTime.of(2026, 9, 5, 7, 5, 9), importer.parseDateTime("2026-09-05 07:05:09", null))
        assertNull(importer.parseDateTime("2026/02/30", null))
    }

    @Test
    fun parseAmount_formats() {
        assertEquals(1200L, importer.parseAmount("¥1,200"))
        assertEquals(1200L, importer.parseAmount("1200円"))
        assertEquals(1200L, importer.parseAmount("-1200"))
        assertNull(importer.parseAmount("12.5"))
    }
}
