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
            2026/09/15,食費,野菜,スーパー,キャベツ,"-1,200",特売
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
        val csv = "Dr.Wallet export\n金額,日付\n-500,2026-09-15\n"

        val result = parse(csv)

        assertEquals(1, result.records.size)
        assertEquals(3, result.records[0].rowNumber)
        assertNull(result.records[0].categoryName)
    }

    @Test
    fun parse_usesTimeColumn() {
        val csv = "日付,時刻,金額\n2026/09/15,08:30,-300\n"

        assertEquals(LocalDateTime.of(2026, 9, 15, 8, 30), parse(csv).records[0].datetime)
    }

    @Test
    fun parse_skipsIncomeAndBadRows() {
        val csv = """
            日付,金額
            2026/09/15,-100
            2026/09/15,200000
            not a date,-100
            2026/09/16,abc
            2026/09/17,0
        """.trimIndent()

        val result = parse(csv)

        assertEquals(listOf(100L), result.records.map { it.amount })
        assertEquals(
            listOf(
                SkippedRow(3, "Income"),
                SkippedRow(4, "Unreadable date: not a date"),
                SkippedRow(5, "Unreadable amount: abc"),
                SkippedRow(6, "Amount is 0"),
            ),
            result.skipped
        )
    }

    @Test
    fun parse_readsDrWalletExport() {
        val csv = """
            取引日,時分,取引金額,内容,ショップ名,電話番号,住所,カテゴリー,口座,ID,集計対象外
            20261011,0000,-60000,"","","","",食費,現金,id1,""
            20261012,0830,-600,"パン","ベーカリー","","",外食費,現金,id2,""
            20261013,0000,250000,"","","","",給与,銀行,id3,""
            20261014,0000,-3000,"","","","",その他,現金,id4,"1"
        """.trimIndent()
        /* 集計対象外 is ignored, so the last row is imported like any other. */

        val result = parse(csv)

        assertEquals(
            listOf(
                ImportRecord(
                    rowNumber = 2,
                    datetime = LocalDateTime.of(2026, 10, 11, 12, 0),
                    amount = 60000,
                    categoryName = "食費",
                    storeName = null,
                    itemName = null,
                    note = null,
                    sourceId = "id1",
                ),
                ImportRecord(
                    rowNumber = 3,
                    datetime = LocalDateTime.of(2026, 10, 12, 8, 30),
                    amount = 600,
                    categoryName = "外食費",
                    storeName = "ベーカリー",
                    itemName = "パン",
                    note = null,
                    sourceId = "id2",
                ),
                ImportRecord(
                    rowNumber = 5,
                    datetime = LocalDateTime.of(2026, 10, 14, 12, 0),
                    amount = 3000,
                    categoryName = "その他",
                    storeName = null,
                    itemName = null,
                    note = null,
                    sourceId = "id4",
                ),
            ),
            result.records
        )
        assertEquals(listOf(SkippedRow(4, "Income")), result.skipped)
    }

    @Test
    fun parse_readsSignAfterCurrencyMarks() {
        val csv = "取引日,取引金額\n20261011,\"¥-1,200\"\n20261012,abc\n20261013,\"1,000円\"\n"

        val result = parse(csv)

        assertEquals(listOf(1200L), result.records.map { it.amount })
        assertEquals(
            listOf(SkippedRow(3, "Unreadable amount: abc"), SkippedRow(4, "Income")),
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
        assertEquals(LocalDateTime.of(2026, 10, 11, 12, 0), importer.parseDateTime("20261011", "0000"))
        assertEquals(LocalDateTime.of(2026, 10, 11, 21, 5), importer.parseDateTime("20261011", "2105"))
        assertNull(importer.parseDateTime("2026/02/30", null))
    }

    @Test
    fun parseSignedAmount_formats() {
        assertEquals(-1200L, importer.parseSignedAmount("¥-1,200"))
        assertEquals(-1200L, importer.parseSignedAmount("-1200円"))
        assertNull(importer.parseSignedAmount("12.5"))
    }

    @Test
    fun parse_positiveAmountIsNotImported() {
        val result = parse("取引日,取引金額\n20261011,1200\n20261012,\"1,200円\"\n")

        assertEquals(emptyList<ImportRecord>(), result.records)
        assertEquals(listOf(SkippedRow(2, "Income"), SkippedRow(3, "Income")), result.skipped)
    }
}
