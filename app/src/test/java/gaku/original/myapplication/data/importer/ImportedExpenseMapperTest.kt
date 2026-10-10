package gaku.original.myapplication.data.importer

import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.GeneratedType
import gaku.original.myapplication.data.dataClass.toGeneratedType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ImportedExpenseMapperTest {

    private val mapper = ImportedExpenseMapper()

    private val tokyo = ZoneId.of("Asia/Tokyo")

    private val food = Category(id = "food", timestamp = 0, name = "食費", enabled = true)
    private val oldFood = Category(id = "oldFood", timestamp = 0, name = "食費", enabled = false)

    private fun record(rowNumber: Int, amount: Long = 150, category: String? = "食費") = ImportRecord(
        rowNumber = rowNumber,
        datetime = LocalDateTime.of(2026, 9, 15, 12, 0),
        amount = amount,
        categoryName = category,
        storeName = "カフェ",
        itemName = "コーヒー",
        note = null,
    )

    @Test
    fun toExpenses_mapsFields() {
        val expense = mapper.toExpenses(listOf(record(2)), "drwallet", tokyo, listOf(food), timestamp = 42).single()

        assertEquals(Instant.parse("2026-09-15T03:00:00Z"), expense.datetime)
        assertEquals(150L, expense.amount)
        assertEquals(food, expense.category)
        assertEquals("カフェ", expense.storeName)
        assertEquals("コーヒー", expense.itemName)
        assertEquals(42L, expense.timestamp)
        assertEquals(GeneratedType.Import("drwallet"), expense.generatedType)
    }

    @Test
    fun toExpenses_sameFileGivesSameIds() {
        val records = listOf(record(2), record(3, amount = 500))

        assertEquals(
            mapper.toExpenses(records, "drwallet", tokyo, listOf(food), 1).map { it.id },
            mapper.toExpenses(records, "drwallet", tokyo, listOf(food), 2).map { it.id }
        )
    }

    @Test
    fun toExpenses_idStartsWithSource() {
        val id = mapper.toExpenses(listOf(record(2)), "drwallet", tokyo, listOf(food), 1).single().id

        assertEquals(true, id.startsWith("drwallet_"))
    }

    @Test
    fun toExpenses_identicalRowsGetDifferentIds() {
        val ids = mapper.toExpenses(listOf(record(2), record(3)), "drwallet", tokyo, listOf(food), 1).map { it.id }

        assertNotEquals(ids[0], ids[1])
    }

    @Test
    fun toExpenses_unknownCategoryIsNull() {
        assertNull(mapper.toExpenses(listOf(record(2, category = "カフェ")), "drwallet", tokyo, listOf(food), 1)[0].category)
    }

    @Test
    fun findCategory_prefersEnabled() {
        assertEquals(food, mapper.findCategory("食費", listOf(oldFood, food)))
    }

    @Test
    fun missingCategoryNames_distinctAndUnknownOnly() {
        val records = listOf(record(2), record(3, category = "カフェ"), record(4, category = "カフェ"), record(5, category = null))

        assertEquals(listOf("カフェ"), mapper.missingCategoryNames(records, listOf(food)))
    }

    @Test
    fun generatedType_roundTrip() {
        val type = GeneratedType.Import("drwallet")
        assertEquals("import___drwallet", type.toSerialized())
        assertEquals(type, type.toSerialized().toGeneratedType())
    }
}
