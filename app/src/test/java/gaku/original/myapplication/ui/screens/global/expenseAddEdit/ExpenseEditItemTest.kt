package gaku.original.myapplication.ui.screens.global.expenseAddEdit

import gaku.original.myapplication.common.AppResult
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.GeneratedType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ExpenseEditItemTest {

    @Test
    fun withRemainingOnLast_setsTotalMinusOthers() {
        val items = listOf(ExpenseEditItem(amount = 300), ExpenseEditItem(amount = 200), ExpenseEditItem(amount = 0))

        val result = items.withRemainingOnLast(1000)

        assertEquals(listOf(300L, 200L, 500L), result.map { it.amount })
    }

    @Test
    fun withRemainingOnLast_neverNegative() {
        val items = listOf(ExpenseEditItem(amount = 1500), ExpenseEditItem(amount = 0))

        assertEquals(0L, items.withRemainingOnLast(1000).last().amount)
    }

    @Test
    fun toExpense_editedItemKeepsIdAndTimestamp() {
        val expense = item(original = sampleExpense).toExpense(datetime, "shop", GeneratedType.Manual, now = 1L)

        assertEquals(sampleExpense.id, expense.id)
        assertEquals(sampleExpense.timestamp, expense.timestamp)
        assertEquals("shop", expense.storeName)
    }

    @Test
    fun toExpense_newItemGetsNewIdAndNow() {
        val expense = item(original = null).toExpense(datetime, "shop", GeneratedType.Manual, now = 1L)

        assertNotEquals(sampleExpense.id, expense.id)
        assertEquals(1L, expense.timestamp)
    }

    @Test
    fun initialUiState_alwaysOneItemList() {
        val zoneId = ZoneId.of("Asia/Tokyo")

        val edit = initialUiState(ExpenseAddEditMode.Edit(sampleExpense), zoneId)
        val new = initialUiState(ExpenseAddEditMode.New(ExpensePrefill(amount = 500)), zoneId)

        assertEquals(listOf(item(original = sampleExpense)), edit.expenseEditList)
        assertEquals(listOf(ExpenseEditItem(amount = 500)), new.expenseEditList)
    }

    @Test
    fun validate_splitTotalMustMatch() {
        val state = ExpenseAddEditUiState(
            selectedDate = LocalDate.of(2026, 9, 15),
            selectedTime = LocalTime.NOON,
            expenseEditList = listOf(item(amount = 300), item(amount = 600)),
            totalAmount = 1000
        )

        assertEquals(
            AppResult.Failure(ExpenseInputError.TotalExpenseNotMatch(1000, 900)),
            state.validate()
        )
        assertEquals(AppResult.Success(Unit), state.copy(totalAmount = 900).validate())
    }

    @Test
    fun validate_singleItemIgnoresTotal() {
        val state = ExpenseAddEditUiState(
            selectedDate = LocalDate.of(2026, 9, 15),
            selectedTime = LocalTime.NOON,
            expenseEditList = listOf(item(amount = 300)),
            totalAmount = 1000
        )

        assertEquals(AppResult.Success(Unit), state.validate())
    }

    private fun item(original: Expense? = null, amount: Long = 1200L) = ExpenseEditItem(
        original = original,
        amount = amount,
        category = sampleExpense.category,
        note = sampleExpense.note,
        productName = sampleExpense.itemName
    )

    companion object {
        private val datetime = Instant.parse("2026-09-15T03:00:00Z")
        private val sampleExpense = Expense(
            id = "expense1",
            generatedType = GeneratedType.Manual,
            datetime = datetime,
            timestamp = 1_780_000_000_000L,
            amount = 1200L,
            category = Category(id = "category1", timestamp = 1L, name = "食費", enabled = true),
            note = "note",
            storeName = "store",
            itemName = "item"
        )
    }
}
