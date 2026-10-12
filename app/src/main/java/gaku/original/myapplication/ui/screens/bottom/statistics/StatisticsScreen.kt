package gaku.original.myapplication.ui.screens.bottom.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.ui.common.EmptyState
import gaku.original.myapplication.ui.common.LoadingBox
import gaku.original.myapplication.ui.common.SectionHeader
import gaku.original.myapplication.ui.common.toCompactYen
import gaku.original.myapplication.ui.common.toYen
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
private val monthAxisFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)

/*
 * Category colors, in this fixed order. Checked for color vision deficiency against the app's light and dark
 * surfaces. Three light colors are below 3:1 contrast, so the values are always also shown as text.
 */
private val lightSeriesColors = listOf(
    Color(0xFF2A78D6), Color(0xFFEB6834), Color(0xFF1BAF7A), Color(0xFFEDA100), Color(0xFFE87BA4)
)
private val darkSeriesColors = listOf(
    Color(0xFF3987E5), Color(0xFFD95926), Color(0xFF199E70), Color(0xFFC98500), Color(0xFFD55181)
)

/* "Other" and the categories inside it. */
private val otherColor = Color(0xFF898781)

/**
 * Colors follow the trend's series, so a category has the same color in the breakdown and the chart.
 * Categories that are not a series of their own are inside "Other".
 */
private class CategoryColors(trend: CategoryTrend?, palette: List<Color>) {
    private val byCategoryId: Map<String?, Color> = trend?.series.orEmpty()
        .filterNot { it.isOther }
        .mapIndexed { index, series -> series.categoryId to palette[index] }
        .toMap()

    fun of(categoryId: String?): Color = byCategoryId[categoryId] ?: otherColor

    fun of(series: TrendSeries): Color = if (series.isOther) otherColor else of(series.categoryId)

    fun hasOwnColor(categoryId: String?): Boolean = categoryId in byCategoryId
}

@Composable
fun StatisticsScreenRoot(
    rootNavController: NavHostController,
    viewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackBarHostState.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    StatisticsScreen(
        uiState = uiState,
        onPreviousMonth = viewModel::onPreviousMonth,
        onNextMonth = viewModel::onNextMonth,
        onRefresh = viewModel::onRefresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    uiState: StatisticsUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onRefresh: () -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val colors = remember(uiState.trend, isDark) {
        CategoryColors(uiState.trend, if (isDark) darkSeriesColors else lightSeriesColors)
    }

    /* Expenses are not updated in real time. Pull down to read them again. */
    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                MonthHeader(
                    month = uiState.selectedMonth,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                )
            }

            val summary = uiState.summary
            val trend = uiState.trend
            if (summary == null || trend == null) {
                item {
                    if (uiState.isLoading) {
                        LoadingBox(modifier = Modifier.height(240.dp))
                    } else {
                        EmptyState(
                            icon = Icons.Outlined.Insights,
                            title = "Statistics could not be loaded",
                            message = "Pull down to try again."
                        )
                    }
                }
                return@LazyColumn
            }

            item {
                SummaryCard(summary = summary)
            }

            item {
                SectionHeader(text = "Breakdown by category", modifier = Modifier.padding(top = 8.dp))
            }
            item {
                BreakdownCard(summary = summary, colors = colors)
            }

            item {
                SectionHeader(
                    text = "Last ${StatisticsCalculator.TREND_MONTHS} months by category",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                TrendCard(trend = trend, colors = colors)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
        }
        Text(
            text = month.format(monthTitleFormatter),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun StatisticsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SummaryCard(summary: MonthSummary) {
    StatisticsCard {
        Column {
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = summary.total.toYen(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            ComparisonRow(total = summary.total, previousTotal = summary.previousTotal)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(modifier = Modifier.fillMaxWidth()) {
            StatItem(label = "Expenses", value = summary.count.toString(), modifier = Modifier.weight(1f))
            StatItem(label = "Daily average", value = summary.dailyAverage.toYen(), modifier = Modifier.weight(1f))
            StatItem(
                label = "Top category",
                value = summary.shares.firstOrNull()?.name ?: "-",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ComparisonRow(total: Long, previousTotal: Long) {
    val difference = total - previousTotal
    val icon = when {
        difference > 0 -> Icons.AutoMirrored.Filled.TrendingUp
        difference < 0 -> Icons.AutoMirrored.Filled.TrendingDown
        else -> Icons.AutoMirrored.Filled.TrendingFlat
    }
    val text = if (previousTotal == 0L) {
        "No expenses last month"
    } else {
        val sign = if (difference > 0) "+" else if (difference < 0) "-" else "±"
        val percent = abs(difference) * 100.0 / previousTotal
        "$sign${abs(difference).toYen()} ($sign${"%.1f".format(percent)}%) vs last month"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (previousTotal != 0L) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BreakdownCard(summary: MonthSummary, colors: CategoryColors) {
    StatisticsCard {
        if (summary.shares.none { it.amount > 0 }) {
            Text(
                text = "No expenses this month.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@StatisticsCard
        }

        ShareBar(shares = summary.shares, colors = colors)

        Column {
            summary.shares.forEach { share ->
                ShareRow(share = share, color = colors.of(share.categoryId))
            }
        }
    }
}

/** The month's total as one bar, split by category. Categories inside "Other" are one segment. */
@Composable
private fun ShareBar(shares: List<CategoryShare>, colors: CategoryColors) {
    val positive = shares.filter { it.amount > 0 }
    val own = positive.filter { colors.hasOwnColor(it.categoryId) }
    val otherRatio = positive.filterNot { colors.hasOwnColor(it.categoryId) }.sumOf { it.ratio.toDouble() }.toFloat()
    val segments = own.map { it.ratio to colors.of(it.categoryId) } +
        if (otherRatio > 0f) listOf(otherRatio to otherColor) else emptyList()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(MaterialTheme.shapes.small),
        /* A gap in the surface color keeps neighboring segments apart. */
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        segments.filter { it.first > 0f }.forEach { (ratio, color) ->
            Box(
                modifier = Modifier
                    .weight(ratio)
                    .fillMaxSize()
                    .background(color)
            )
        }
    }
}

@Composable
private fun ShareRow(share: CategoryShare, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = share.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (share.categoryId == null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (share.count == 1) "1 expense" else "${share.count} expenses",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "%.1f%%".format(share.ratio * 100),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = share.amount.toYen(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(96.dp)
        )
    }
}

@Composable
private fun TrendCard(trend: CategoryTrend, colors: CategoryColors) {
    /* The newest month is selected first. Tap or drag on the chart to select another. */
    var selectedIndex by remember(trend.months) { mutableIntStateOf(trend.months.lastIndex) }

    StatisticsCard {
        if (trend.series.isEmpty()) {
            Text(
                text = "No expenses in these months.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@StatisticsCard
        }

        TrendChart(
            trend = trend,
            colors = colors,
            selectedIndex = selectedIndex,
            onSelect = { selectedIndex = it },
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        /* The legend, with the values of the selected month. It is also the text view of the chart. */
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = trend.months[selectedIndex].format(monthTitleFormatter),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            trend.series.forEach { series ->
                LegendRow(
                    name = series.name,
                    amount = series.totals[selectedIndex],
                    color = colors.of(series),
                )
            }
            LegendRow(
                name = "Total",
                amount = trend.series.sumOf { it.totals[selectedIndex] },
                color = null,
            )
        }
    }
}

@Composable
private fun LegendRow(name: String, amount: Long, color: Color?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(3.dp)
                .background(color ?: Color.Transparent, CircleShape)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (color == null) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = amount.toYen(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (color == null) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/** A line per series over the months, with a shared y axis from zero. */
@Composable
private fun TrendChart(
    trend: CategoryTrend,
    colors: CategoryColors,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val selectedLabelStyle = labelStyle.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLow
    val crosshairColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    val maxValue = trend.series.maxOf { series -> series.totals.max() }.coerceAtLeast(0L)
    val axis = remember(maxValue) { niceAxis(maxValue) }
    val monthCount = trend.months.size

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .pointerInput(monthCount) {
                detectTapGestures { offset -> onSelect(indexAt(offset.x, size.width.toFloat(), monthCount)) }
            }
            .pointerInput(monthCount) {
                detectHorizontalDragGestures { change, _ ->
                    onSelect(indexAt(change.position.x, size.width.toFloat(), monthCount))
                }
            }
    ) {
        val yLabels = axis.ticks.map { textMeasurer.measure(it.toCompactYen(), labelStyle) }
        val left = (yLabels.maxOf { it.size.width } + 8.dp.toPx())
        val xLabelHeight = textMeasurer.measure("May", labelStyle).size.height
        val top = 8.dp.toPx()
        val bottom = size.height - xLabelHeight - 8.dp.toPx()
        val plotWidth = size.width - left
        fun x(index: Int) = left + plotWidth * (index + 0.5f) / monthCount
        fun y(value: Long) = bottom - (bottom - top) * (value.coerceAtLeast(0L).toFloat() / axis.max)

        /* Grid and y labels. */
        axis.ticks.forEachIndexed { i, tick ->
            val tickY = y(tick)
            drawLine(
                color = gridColor,
                start = Offset(left, tickY),
                end = Offset(size.width, tickY),
                strokeWidth = if (tick == 0L) 1.5.dp.toPx() else 1.dp.toPx()
            )
            val label = yLabels[i]
            drawText(label, topLeft = Offset(left - 8.dp.toPx() - label.size.width, tickY - label.size.height / 2f))
        }

        /* The selected month. */
        drawLine(
            color = crosshairColor,
            start = Offset(x(selectedIndex), top),
            end = Offset(x(selectedIndex), bottom),
            strokeWidth = 1.dp.toPx()
        )

        /* Month labels. */
        trend.months.forEachIndexed { index, month ->
            val label = textMeasurer.measure(
                month.format(monthAxisFormatter),
                if (index == selectedIndex) selectedLabelStyle else labelStyle
            )
            drawText(label, topLeft = Offset(x(index) - label.size.width / 2f, bottom + 6.dp.toPx()))
        }

        /* "Other" first so the categories are drawn over it. */
        trend.series.sortedByDescending { it.isOther }.forEach { series ->
            val color = colors.of(series)
            val path = Path()
            series.totals.forEachIndexed { index, value ->
                if (index == 0) path.moveTo(x(index), y(value)) else path.lineTo(x(index), y(value))
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            series.totals.forEachIndexed { index, value ->
                val center = Offset(x(index), y(value))
                val radius = if (index == selectedIndex) 5.dp.toPx() else 4.dp.toPx()
                /* A ring in the surface color keeps overlapping markers apart. */
                drawCircle(color = surfaceColor, radius = radius + 2.dp.toPx(), center = center)
                drawCircle(color = color, radius = radius, center = center)
            }
        }
    }
}

private fun indexAt(x: Float, width: Float, count: Int): Int =
    ((x / width) * count).toInt().coerceIn(0, count - 1)

private data class ChartAxis(val max: Long, val ticks: List<Long>)

/** A y axis from zero with four steps of 1, 2 or 5 times a power of ten. */
private fun niceAxis(maxValue: Long): ChartAxis {
    if (maxValue <= 0L) return ChartAxis(max = 1_000L, ticks = listOf(0L, 500L, 1_000L))
    val rawStep = maxValue / 4.0
    val magnitude = 10.0.pow(floor(log10(rawStep)))
    val step = listOf(1.0, 2.0, 5.0, 10.0).first { it * magnitude >= rawStep } * magnitude
    val stepCount = ceil(maxValue / step).roundToInt()
    val ticks = (0..stepCount).map { (it * step).toLong() }
    return ChartAxis(max = ticks.last(), ticks = ticks)
}

@Preview(showBackground = true)
@Composable
fun StatisticsScreenPreview() {
    val month = YearMonth.of(2026, 10)
    val months = StatisticsCalculator.trendMonths(month)
    val uiState = StatisticsUiState(
        selectedMonth = month,
        summary = MonthSummary(
            month = month,
            total = 182_400,
            count = 64,
            previousTotal = 171_000,
            dailyAverage = 15_200,
            shares = listOf(
                CategoryShare("food", "Food", 72_000, 0.395f, 30),
                CategoryShare("home", "Rent", 60_000, 0.329f, 1),
                CategoryShare("util", "Utilities", 21_400, 0.117f, 3),
                CategoryShare("book", "Books", 14_000, 0.077f, 9),
                CategoryShare("hobby", "Hobby", 9_000, 0.049f, 6),
                CategoryShare(null, "Uncategorized", 6_000, 0.033f, 15),
            )
        ),
        trend = CategoryTrend(
            months = months,
            series = listOf(
                TrendSeries("food", "Food", false, listOf(65_000, 70_000, 81_000, 76_000, 68_000, 72_000)),
                TrendSeries("home", "Rent", false, listOf(60_000, 60_000, 60_000, 60_000, 60_000, 60_000)),
                TrendSeries("util", "Utilities", false, listOf(24_000, 19_000, 26_000, 31_000, 23_000, 21_400)),
                TrendSeries("book", "Books", false, listOf(8_000, 12_000, 5_000, 9_000, 11_000, 14_000)),
                TrendSeries(null, "Other", true, listOf(12_000, 9_000, 18_000, 7_000, 9_000, 15_000)),
            )
        )
    )
    StatisticsScreen(uiState = uiState, onPreviousMonth = {}, onNextMonth = {}, onRefresh = {})
}
