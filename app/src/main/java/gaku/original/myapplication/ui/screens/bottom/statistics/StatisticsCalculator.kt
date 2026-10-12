package gaku.original.myapplication.ui.screens.bottom.statistics

import gaku.original.myapplication.data.dataClass.Expense
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/** One category's part of a month's total. A null [categoryId] is "Uncategorized". */
data class CategoryShare(
    val categoryId: String?,
    val name: String,
    val amount: Long,
    /* 0.0 to 1.0 of the month's total. */
    val ratio: Float,
    val count: Int,
)

data class MonthSummary(
    val month: YearMonth,
    val total: Long,
    val count: Int,
    val previousTotal: Long,
    /* The total divided by the days passed in the month, or by all its days for a past or future month. */
    val dailyAverage: Long,
    /* Largest first. */
    val shares: List<CategoryShare>,
)

/**
 * The total of each category in each month. [isOther] is the sum of the categories that are not shown on their own.
 * A null [categoryId] is "Uncategorized".
 */
data class TrendSeries(
    val categoryId: String?,
    val name: String,
    val isOther: Boolean,
    /* One per month of CategoryTrend.months. */
    val totals: List<Long>,
)

data class CategoryTrend(
    /* Oldest first. */
    val months: List<YearMonth>,
    /* Largest total over all months first, "Other" last. */
    val series: List<TrendSeries>,
)

object StatisticsCalculator {
    const val TREND_MONTHS = 6

    /* More lines than this cannot be told apart by color, so the rest are added up as "Other". */
    const val MAX_TREND_CATEGORIES = 5

    const val UNCATEGORIZED_NAME = "Uncategorized"
    const val OTHER_NAME = "Other"

    /** The [TREND_MONTHS] months ending with [month], oldest first. */
    fun trendMonths(month: YearMonth): List<YearMonth> =
        (TREND_MONTHS - 1 downTo 0).map { month.minusMonths(it.toLong()) }

    fun monthOf(expense: Expense, zoneId: ZoneId): YearMonth =
        YearMonth.from(expense.datetime.atZone(zoneId))

    /**
     * @param expenses the expenses of [month].
     * @param categoryNames current category names by id. An expense keeps the name it was saved with
     *   when its category was deleted.
     */
    fun summarize(
        month: YearMonth,
        expenses: List<Expense>,
        previousMonthExpenses: List<Expense>,
        categoryNames: Map<String, String>,
        today: LocalDate,
    ): MonthSummary {
        val total = expenses.sumOf { it.amount }

        val shares = expenses
            .groupBy { it.category?.id }
            .map { (categoryId, categoryExpenses) ->
                val amount = categoryExpenses.sumOf { it.amount }
                CategoryShare(
                    categoryId = categoryId,
                    name = nameOf(categoryExpenses.first(), categoryNames),
                    amount = amount,
                    ratio = if (total > 0) amount.toFloat() / total else 0f,
                    count = categoryExpenses.size,
                )
            }
            .sortedWith(compareByDescending<CategoryShare> { it.amount }.thenBy { it.name })

        val days = if (YearMonth.from(today) == month) today.dayOfMonth else month.lengthOfMonth()

        return MonthSummary(
            month = month,
            total = total,
            count = expenses.size,
            previousTotal = previousMonthExpenses.sumOf { it.amount },
            dailyAverage = total / days,
            shares = shares,
        )
    }

    /**
     * @param expensesByMonth the expenses of each of [months].
     * @param categoryNames see [summarize].
     */
    fun trend(
        months: List<YearMonth>,
        expensesByMonth: Map<YearMonth, List<Expense>>,
        categoryNames: Map<String, String>,
    ): CategoryTrend {
        val all = months.flatMap { expensesByMonth[it].orEmpty() }

        val ranked = all
            .groupBy { it.category?.id }
            .map { (categoryId, categoryExpenses) ->
                Triple(categoryId, nameOf(categoryExpenses.first(), categoryNames), categoryExpenses.sumOf { it.amount })
            }
            .sortedWith(compareByDescending<Triple<String?, String, Long>> { it.third }.thenBy { it.second })

        /* "Other" takes the last line only when it stands for two or more categories. */
        val shownCount = if (ranked.size <= MAX_TREND_CATEGORIES) ranked.size else MAX_TREND_CATEGORIES - 1
        val shown = ranked.take(shownCount)
        val shownIds = shown.map { it.first }.toSet()

        fun totalsOf(predicate: (Expense) -> Boolean): List<Long> =
            months.map { month -> expensesByMonth[month].orEmpty().filter(predicate).sumOf { it.amount } }

        val series = shown.map { (categoryId, name, _) ->
            TrendSeries(
                categoryId = categoryId,
                name = name,
                isOther = false,
                totals = totalsOf { it.category?.id == categoryId },
            )
        }

        val other = if (ranked.size > shownCount) {
            listOf(
                TrendSeries(
                    categoryId = null,
                    name = OTHER_NAME,
                    isOther = true,
                    totals = totalsOf { it.category?.id !in shownIds },
                )
            )
        } else {
            emptyList()
        }

        return CategoryTrend(months = months, series = series + other)
    }

    private fun nameOf(expense: Expense, categoryNames: Map<String, String>): String {
        val category = expense.category ?: return UNCATEGORIZED_NAME
        return categoryNames[category.id] ?: category.name
    }
}
