package gaku.original.myapplication.ui.screens.bottom.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.common.AppResult
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.ui.common.EmptyState
import gaku.original.myapplication.ui.common.toCompactYen
import gaku.original.myapplication.ui.common.toYen
import gaku.original.myapplication.ui.screens.global.expenseAddEdit.ExpenseAddEditMode
import kotlinx.coroutines.flow.distinctUntilChanged
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import timber.log.Timber
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
private val expenseTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    rootNavController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackBarHostState.current

    val isWide = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(
        uiState.message
    ) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    HomeScreen(
        uiState = uiState,
        isWide = isWide,
        onMonthChanged = {
            viewModel.onMonthChanged(it)
        },
        onExpenseClick = {
            when (val ret = viewModel.onExpenseClick(it)) {
                is AppResult.Failure -> {
                    /* snackbar is triggered through uiState */
                    Timber.d("Failed to start editing expense:${it}")
                }

                is AppResult.Success -> {
                    rootNavController.navigate(
                        MainGraph.Global.ExpenseAddEdit(
                            ExpenseAddEditMode.Edit(ret.value)
                        )
                    )
                }
            }
        },
        onDayClick = {
            val prefill = viewModel.onDayClick(it)
            rootNavController.navigate(
                MainGraph.Global.ExpenseAddEdit(
                    ExpenseAddEditMode.New(prefill)
                )
            )
        },
        onFABClick = {
            rootNavController.navigate(
                MainGraph.Global.ExpenseAddEdit()
            )
        }
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    isWide: Boolean = false,
    onMonthChanged: (YearMonth) -> Unit,
    onExpenseClick: (String?) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onFABClick: () -> Unit
) {

    val initialMonth = remember { uiState.selectedMonth }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(50) } // Adjust as needed
    val endMonth = remember { currentMonth.plusMonths(50) } // Adjust as needed
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // Available from the library

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = initialMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    val lazyListState = rememberLazyListState()

    LaunchedEffect(calendarState) {
        snapshotFlow {
            calendarState.firstVisibleMonth.yearMonth
        }.distinctUntilChanged()
            .collect { month ->
                Timber.d("Month changed to $month")
                onMonthChanged(month)
            }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isWide) {
            Timber.d("This is wide layout")
            /* Screen is wide. ex:tablet, rotated smartphone */
            /* Screen is narrow. ex: smartphone */
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                HomeHorizontalCalendar(
                    uiState = uiState,
                    calendarState = calendarState,
                    modifier = Modifier.weight(1f),
                    onDayClick = onDayClick
                )

                LazyExpensesColumn(
                    uiState = uiState,
                    lazyLisState = lazyListState,
                    modifier = Modifier.weight(1f),
                    onExpenseClick = {
                        onExpenseClick(it)
                    }
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                HomeHorizontalCalendar(
                    uiState = uiState,
                    calendarState = calendarState,
                    modifier = Modifier.weight(1.3f),
                    onDayClick = onDayClick
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                LazyExpensesColumn(
                    uiState = uiState,
                    lazyLisState = lazyListState,
                    modifier = Modifier.weight(1f),
                    onExpenseClick = {
                        onExpenseClick(it)
                    }
                )
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(
                    Alignment.BottomEnd
                )
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = {
                onFABClick()
            }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Expense"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val uiState = HomeUiState(
        isLoading = false,
        shownExpenses = listOf(
            ExpenseUi(
                id = "1",
                amount = 1200,
                datetime = Instant.now().atZone(ZoneId.of("Asia/Tokyo")).toLocalDateTime(),
                category = null
            ),
            ExpenseUi(
                id = "2",
                amount = 2400,
                datetime = Instant.now().atZone(ZoneId.of("Asia/Tokyo")).toLocalDateTime(),
                category = null
            ),
            ExpenseUi(
                id = "3",
                amount = 3400,
                datetime = Instant.now().atZone(ZoneId.of("Asia/Tokyo")).toLocalDateTime(),
                category = Category(
                    id = "食費",
                    timestamp = 0L,
                    name = "食費",
                    enabled = true
                )
            ),
        ),
        message = null,
        estimatedAmount = 10.0,
        monthlyTotal = 10000,
        dailyAmounts = mapOf(
            LocalDate.now() to 1000,
            LocalDate.now().minusDays(1) to 2000,
            LocalDate.now().minusDays(2) to 3000
        )
    )

    HomeScreen(
        uiState = uiState,
        onMonthChanged = {},
        onExpenseClick = {},
        onDayClick = {},
        onFABClick = {}
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HomeHorizontalCalendar(
    uiState: HomeUiState,
    calendarState: CalendarState,
    modifier: Modifier = Modifier,
    onDayClick: (LocalDate) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        MonthSummaryHeader(uiState = uiState)

        DaysOfWeekTitle(
            daysOfWeek = daysOfWeek()
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        ) {
            /* height of the Day is calculated by maxHeight */
            val dayHeight = maxHeight / 6
            HorizontalCalendar(
                state = calendarState,
                modifier = Modifier.fillMaxSize(),
                dayContent = {
                    Day(
                        modifier = Modifier.height(dayHeight),
                        day = it,
                        price = uiState.dailyAmounts[it.date] ?: 0L,
                        isToday = it.date == LocalDate.now(),
                        onDayClick = {
                            onDayClick(it.date)
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun MonthSummaryHeader(uiState: HomeUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = uiState.selectedMonth.format(monthTitleFormatter),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = uiState.monthlyTotal.toYen(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            uiState.estimatedAmount?.let {
                Text(
                    text = "Estimated ${it.toLong().toYen()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LazyExpensesColumn(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    lazyLisState: LazyListState,
    onExpenseClick: (String?) -> Unit
) {
    Column(modifier = modifier) {
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (!uiState.isLoading && uiState.shownExpenses.isEmpty()) {
            EmptyState(
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                title = "No expenses this month",
                message = "Tap a day or the + button to add one."
            )
            return@Column
        }

        LazyColumnScrollbar(
            state = lazyLisState,
            settings = ScrollbarSettings.Default.copy(
                alwaysShowScrollbar = true,
                thumbUnselectedColor = MaterialTheme.colorScheme.outlineVariant,
                thumbSelectedColor = MaterialTheme.colorScheme.primary
            )
        ) {
            LazyColumn(
                state = lazyLisState,
                modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = true
            ) {
                items(uiState.shownExpenses) { expense ->
                    /* if it is today, highlight the background */
                    val isToday =
                        expense.datetime?.toLocalDate() == LocalDate.now(uiState.zoneId)
                    ExpenseItem(
                        expenseUi = expense,
                        isToday = isToday,
                        onEdit = {
                            /* idだけ渡してAdd/Editする */
                            onExpenseClick(expense.id)
                        },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 72.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                }
                /* keep the last row clear of the FAB */
                item { Spacer(modifier = Modifier.height(88.dp)) }
            }
        }
    }
}

private val weekend = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

@Composable
private fun dayOfWeekColor(dayOfWeek: DayOfWeek): Color = when (dayOfWeek) {
    DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error
    DayOfWeek.SATURDAY -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = dayOfWeekColor(dayOfWeek)
            )
        }
    }
}

@Composable
fun Day(
    modifier: Modifier = Modifier,
    day: CalendarDay,
    price: Long,
    isToday: Boolean = false,
    onDayClick: () -> Unit
) {
    val isCurrentMonth = day.position == DayPosition.MonthDate
    val hasExpense = isCurrentMonth && price > 0

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(MaterialTheme.shapes.small)
            .then(
                if (hasExpense) {
                    Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                } else {
                    Modifier
                }
            )
            .clickable(
                enabled = isCurrentMonth,
                onClick = {
                    onDayClick()
                }
            )
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .then(
                        if (isToday) {
                            Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${day.date.dayOfMonth}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isToday -> MaterialTheme.colorScheme.onPrimary
                        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        day.date.dayOfWeek in weekend -> dayOfWeekColor(day.date.dayOfWeek)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            if (hasExpense) {
                /* when the value is too big, use the expression like 12.3K */
                Text(
                    price.toCompactYen(),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 1.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ExpenseItem(
    expenseUi: ExpenseUi,
    isToday: Boolean = false,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onEdit()
            }
            .then(
                if (isToday) {
                    Modifier.background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        /* date badge */
        Column(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${expenseUi.datetime?.dayOfMonth ?: "-"}",
                style = MaterialTheme.typography.titleSmall,
                color = if (isToday) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expenseUi.category?.name ?: "Uncategorized",
                style = MaterialTheme.typography.bodyLarge,
                color = if (expenseUi.category == null) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            expenseUi.datetime?.let {
                Text(
                    text = "${
                        it.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    } ${it.format(expenseTimeFormatter)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = expenseUi.amount?.toYen() ?: "-",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
