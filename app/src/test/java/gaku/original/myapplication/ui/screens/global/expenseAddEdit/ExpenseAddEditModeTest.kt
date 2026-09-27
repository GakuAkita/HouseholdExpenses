package gaku.original.myapplication.ui.screens.global.expenseAddEdit

import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.GeneratedType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

/** ExpenseAddEditMode is passed as a navigation argument in JSON. */
class ExpenseAddEditModeTest {

    @Test
    fun new_jsonRoundTrip() {
        val mode: ExpenseAddEditMode = ExpenseAddEditMode.New(
            ExpensePrefill(
                datetime = Instant.parse("2026-09-15T03:00:00Z"),
                amount = 1200L,
                storeName = "store",
                generatedType = GeneratedType.MailExtraction(templateTypeName = "amazon_item")
            )
        )

        assertEquals(mode, roundTrip(mode))
    }

    @Test
    fun newWithoutPrefill_jsonRoundTrip() {
        val mode: ExpenseAddEditMode = ExpenseAddEditMode.New()

        assertEquals(mode, roundTrip(mode))
    }

    @Test
    fun edit_jsonRoundTrip() {
        val mode: ExpenseAddEditMode = ExpenseAddEditMode.Edit(
            Expense(
                id = "expense1",
                generatedType = GeneratedType.RepeatAdd(repeatAddId = "repeat1"),
                datetime = Instant.parse("2026-09-15T03:00:00Z"),
                timestamp = 1_780_000_000_000L,
                amount = 1200L,
                category = Category(id = "category1", timestamp = 1L, name = "食費", enabled = true),
                note = "note",
                storeName = "store",
                itemName = "item"
            )
        )

        assertEquals(mode, roundTrip(mode))
    }

    private fun roundTrip(mode: ExpenseAddEditMode): ExpenseAddEditMode =
        Json.decodeFromString(Json.encodeToString(mode))
}
