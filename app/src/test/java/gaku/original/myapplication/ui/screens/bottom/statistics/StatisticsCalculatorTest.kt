package gaku.original.myapplication.ui.screens.bottom.statistics

import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.GeneratedType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class StatisticsCalculatorTest {

    private val food = Category(id = "food", timestamp = 1L, name = "Food", enabled = true)
    private val rent = Category(id = "rent", timestamp = 2L, name = "Rent", enabled = true)
    private val october = YearMonth.of(2026, 10)
    private val pastDay = LocalDate.of(2027, 1, 1)

    @Test
    fun trendMonths_areTheSixMonthsEndingWithTheMonth_oldestFirst() {
        assertEquals(
            listOf(5, 6, 7, 8, 9, 10).map { YearMonth.of(2026, it) },
            StatisticsCalculator.trendMonths(october)
        )
    }

    @Test
    fun monthOf_usesTheAppTimeZone() {
        /* 2026-09-30 15:00 UTC is 2026-10-01 00:00 in Tokyo. */
        val expense = expense(datetime = "2026-09-30T15:00:00Z")

        assertEquals(october, StatisticsCalculator.monthOf(expense, ZoneId.of("Asia/Tokyo")))
        assertEquals(YearMonth.of(2026, 9), StatisticsCalculator.monthOf(expense, ZoneId.of("UTC")))
    }

    @Test
    fun summarize_sharesAreLargestFirstWithRatios() {
        val summary = StatisticsCalculator.summarize(
            month = october,
            expenses = listOf(
                expense(amount = 1_000, category = food),
                expense(amount = 2_000, category = food),
                expense(amount = 6_000, category = rent),
                expense(amount = 1_000, category = null),
            ),
            previousMonthExpenses = listOf(expense(amount = 8_000, category = food)),
            categoryNames = emptyMap(),
            today = pastDay,
        )

        assertEquals(10_000L, summary.total)
        assertEquals(4, summary.count)
        assertEquals(8_000L, summary.previousTotal)
        assertEquals(listOf("Rent", "Food", "Uncategorized"), summary.shares.map { it.name })
        assertEquals(listOf(6_000L, 3_000L, 1_000L), summary.shares.map { it.amount })
        assertEquals(listOf(0.6f, 0.3f, 0.1f), summary.shares.map { it.ratio })
        assertEquals(listOf(1, 2, 1), summary.shares.map { it.count })
        assertEquals(null, summary.shares.last().categoryId)
    }

    @Test
    fun summarize_usesCurrentCategoryNamesAndKeepsSavedNameOfDeletedCategory() {
        val deleted = Category(id = "deleted", timestamp = 3L, name = "Old name", enabled = true)

        val summary = StatisticsCalculator.summarize(
            month = october,
            expenses = listOf(expense(amount = 2_000, category = food), expense(amount = 1_000, category = deleted)),
            previousMonthExpenses = emptyList(),
            categoryNames = mapOf("food" to "Groceries"),
            today = pastDay,
        )

        assertEquals(listOf("Groceries", "Old name"), summary.shares.map { it.name })
    }

    @Test
    fun summarize_dailyAverageUsesDaysPassedInTheCurrentMonth() {
        val expenses = listOf(expense(amount = 31_000, category = food))

        val current = StatisticsCalculator.summarize(
            october, expenses, emptyList(), emptyMap(), today = LocalDate.of(2026, 10, 10)
        )
        val past = StatisticsCalculator.summarize(october, expenses, emptyList(), emptyMap(), today = pastDay)

        assertEquals(3_100L, current.dailyAverage)
        assertEquals(1_000L, past.dailyAverage)
    }

    @Test
    fun summarize_topExpensesAreTheLargestThreeOfTheCategory_olderFirstAmongEqualAmounts() {
        val small = expense(amount = 100, category = food)
        val large = expense(amount = 5_000, category = food)
        val newerTie = expense(amount = 2_000, category = food, datetime = "2026-10-20T00:00:00Z")
        val olderTie = expense(amount = 2_000, category = food, datetime = "2026-10-03T00:00:00Z")
        val rentOnly = expense(amount = 20_000, category = rent)

        val summary = StatisticsCalculator.summarize(
            october, listOf(small, newerTie, large, rentOnly, olderTie), emptyList(), emptyMap(), pastDay
        )

        assertEquals(listOf(rentOnly), summary.shares[0].topExpenses)
        assertEquals(listOf(large, olderTie, newerTie), summary.shares[1].topExpenses)
    }

    @Test
    fun summarize_emptyMonth() {
        val summary = StatisticsCalculator.summarize(october, emptyList(), emptyList(), emptyMap(), pastDay)

        assertEquals(0L, summary.total)
        assertEquals(0L, summary.dailyAverage)
        assertTrue(summary.shares.isEmpty())
    }

    @Test
    fun trend_givesTotalsPerMonthForEachCategory_largestFirst() {
        val months = StatisticsCalculator.trendMonths(october)
        val trend = StatisticsCalculator.trend(
            months = months,
            expensesByMonth = mapOf(
                YearMonth.of(2026, 9) to listOf(expense(amount = 1_000, category = food)),
                october to listOf(
                    expense(amount = 2_000, category = food),
                    expense(amount = 500, category = food),
                    expense(amount = 6_000, category = rent),
                ),
            ),
            categoryNames = emptyMap(),
        )

        assertEquals(months, trend.months)
        assertEquals(listOf("Rent", "Food"), trend.series.map { it.name })
        assertEquals(listOf(0L, 0L, 0L, 0L, 0L, 6_000L), trend.series[0].totals)
        assertEquals(listOf(0L, 0L, 0L, 0L, 1_000L, 2_500L), trend.series[1].totals)
        assertFalse(trend.series.any { it.isOther })
    }

    @Test
    fun trend_foldsSmallCategoriesIntoOther() {
        val categories = (1..7).map { Category(id = "c$it", timestamp = it.toLong(), name = "C$it", enabled = true) }
        /* c1 is the largest, c7 the smallest. */
        val expenses = categories.mapIndexed { index, category -> expense(amount = (10 - index) * 1_000L, category = category) }

        val trend = StatisticsCalculator.trend(
            months = StatisticsCalculator.trendMonths(october),
            expensesByMonth = mapOf(october to expenses),
            categoryNames = emptyMap(),
        )

        assertEquals(StatisticsCalculator.MAX_TREND_CATEGORIES, trend.series.size)
        assertEquals(listOf("C1", "C2", "C3", "C4", "Other"), trend.series.map { it.name })
        assertTrue(trend.series.last().isOther)
        /* c5, c6 and c7: 6,000 + 5,000 + 4,000 */
        assertEquals(15_000L, trend.series.last().totals.last())
    }

    @Test
    fun trend_showsEveryCategoryWhenTheyFit() {
        val categories = (1..5).map { Category(id = "c$it", timestamp = it.toLong(), name = "C$it", enabled = true) }

        val trend = StatisticsCalculator.trend(
            months = StatisticsCalculator.trendMonths(october),
            expensesByMonth = mapOf(october to categories.map { expense(amount = 1_000, category = it) }),
            categoryNames = emptyMap(),
        )

        assertEquals(5, trend.series.size)
        assertFalse(trend.series.any { it.isOther })
    }

    @Test
    fun trend_otherIsNotConfusedWithUncategorized() {
        val categories = (1..6).map { Category(id = "c$it", timestamp = it.toLong(), name = "C$it", enabled = true) }
        val expenses = categories.map { expense(amount = 10_000, category = it) } + expense(amount = 50_000, category = null)

        val trend = StatisticsCalculator.trend(
            months = StatisticsCalculator.trendMonths(october),
            expensesByMonth = mapOf(october to expenses),
            categoryNames = emptyMap(),
        )

        assertEquals("Uncategorized", trend.series.first().name)
        assertFalse(trend.series.first().isOther)
        /* c4, c5 and c6 */
        assertEquals(30_000L, trend.series.last().totals.last())
    }

    private fun expense(
        amount: Long = 1_000,
        category: Category? = food,
        datetime: String = "2026-10-15T00:00:00Z",
    ) = Expense(
        id = "id-${counter++}",
        generatedType = GeneratedType.Manual,
        datetime = Instant.parse(datetime),
        timestamp = 1L,
        amount = amount,
        category = category,
        note = null,
        storeName = null,
        itemName = null,
    )

    private var counter = 0
}
