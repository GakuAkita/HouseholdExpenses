package gaku.original.myapplication.data.importer

import java.time.LocalDateTime

/** Ignores the file and returns sample rows. */
class FakeExpenseImporter : ExpenseImporter {
    override val source: String = "fake"

    override fun parse(bytes: ByteArray): ImportParseResult = ImportParseResult(
        records = listOf(
            ImportRecord(2, LocalDateTime.of(2026, 9, 15, 12, 0), 1200, "食費", "スーパー", "野菜", null),
            ImportRecord(3, LocalDateTime.of(2026, 9, 16, 12, 0), 150, "カフェ", "コーヒー店", null, null),
        ),
        skipped = listOf(SkippedRow(4, "Income")),
    )
}
