package gaku.original.myapplication.data.dataClass

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ExpenseSearchFilterTest {

    private val food = Category(id = "food", timestamp = 1L, name = "食費", enabled = true)

    @Test
    fun emptyFilter_matchesEverything() {
        val filter = ExpenseSearchFilter()

        assertTrue(filter.isEmpty())
        assertTrue(filter.matches(expense()))
        assertTrue(filter.matches(expense(category = food)))
    }

    @Test
    fun uncategorized_matchesOnlyExpensesWithoutCategory() {
        val filter = ExpenseSearchFilter.Uncategorized

        assertTrue(filter.matches(expense(category = null)))
        assertFalse(filter.matches(expense(category = food)))
    }

    @Test
    fun categoryIds_matchesAnyOfTheCategoriesOrUncategorized() {
        val filter = ExpenseSearchFilter(categoryIds = setOf(null, "food"))

        assertTrue(filter.matches(expense(category = null)))
        assertTrue(filter.matches(expense(category = food)))
        assertFalse(filter.matches(expense(category = food.copy(id = "daily"))))
    }

    @Test
    fun generatedTypes_matchesByKindRegardlessOfPayload() {
        val filter = ExpenseSearchFilter(
            generatedTypes = setOf(GeneratedTypeKind.RepeatAdd, GeneratedTypeKind.MailExtraction)
        )

        assertTrue(filter.matches(expense(generatedType = GeneratedType.RepeatAdd("repeat1"))))
        assertTrue(filter.matches(expense(generatedType = GeneratedType.MailExtraction("amazon_item"))))
        assertFalse(filter.matches(expense(generatedType = GeneratedType.Manual)))
    }

    @Test
    fun keyword_partiallyMatchesStoreItemOrNoteIgnoringCase() {
        val filter = ExpenseSearchFilter(keyword = " amazon ")

        assertTrue(filter.matches(expense(storeName = "Amazon.co.jp")))
        assertTrue(filter.matches(expense(itemName = "amazon gift")))
        assertTrue(filter.matches(expense(note = "bought on AMAZON")))
        assertFalse(filter.matches(expense(storeName = "Rakuten")))
        assertFalse(filter.matches(expense()))
    }

    @Test
    fun conditions_areCombinedWithAnd() {
        val filter = ExpenseSearchFilter(
            categoryIds = setOf(null),
            generatedTypes = setOf(GeneratedTypeKind.MailExtraction)
        )

        assertTrue(filter.matches(expense(generatedType = GeneratedType.MailExtraction("udemy"))))
        assertFalse(filter.matches(expense(generatedType = GeneratedType.Manual)))
        assertFalse(
            filter.matches(
                expense(category = food, generatedType = GeneratedType.MailExtraction("udemy"))
            )
        )
    }

    private fun expense(
        category: Category? = null,
        generatedType: GeneratedType = GeneratedType.Manual,
        storeName: String? = null,
        itemName: String? = null,
        note: String? = null,
    ) = Expense(
        id = "expense1",
        generatedType = generatedType,
        datetime = Instant.parse("2026-09-15T03:00:00Z"),
        timestamp = 0L,
        amount = 1200L,
        category = category,
        note = note,
        storeName = storeName,
        itemName = itemName,
    )
}
