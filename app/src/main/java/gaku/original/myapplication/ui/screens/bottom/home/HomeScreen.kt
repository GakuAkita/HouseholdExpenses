package gaku.original.myapplication.ui.screens.bottom.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.flow.distinctUntilChanged
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import timber.log.Timber
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    rootNavController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
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
                            ret.value
                        )
                    )
                }
            }
        },
        onFABClick = {
            // Honestly, I just want to pass null.
            // But, as long as I use "navTypeOf" inline function, it seems to be impossible.
            // I just pass Expense whose id is null, which is used to identify add or edit.
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
                    modifier = Modifier.weight(1f)
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
                    modifier = Modifier.weight(1f)
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
        }

        FloatingActionButton(
            modifier = Modifier
                .align(
                    Alignment.BottomEnd
                )
                .padding(vertical = 16.dp, horizontal = 4.dp),
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
                    name = "食費"
                )
            ),
        ),
        message = null,
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
        onFABClick = {}
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HomeHorizontalCalendar(
    uiState: HomeUiState,
    calendarState: CalendarState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("${uiState.selectedMonth.year}-${uiState.selectedMonth.monthValue}")
            Spacer(modifier = Modifier.padding(10.dp))
            Column {
                Text("Monthly Total:${uiState.monthlyTotal} yen")
            }
        }

        DaysOfWeekTitle(
            daysOfWeek = daysOfWeek()
        )

        BoxWithConstraints(
            modifier = Modifier.weight(1f)
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
                        onDayClick = {}
                    )
                }
            )
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
    LazyColumnScrollbar(
        modifier = modifier,
        state = lazyLisState,
        settings = ScrollbarSettings.Default.copy(
            alwaysShowScrollbar = true,
            thumbUnselectedColor = MaterialTheme.colorScheme.secondary,
            thumbSelectedColor = MaterialTheme.colorScheme.primary
        )
    ) {
        LazyColumn(
            state = lazyLisState,
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = true
        ) {
            items(uiState.shownExpenses) { expense ->
                ExpenseItem(
                    expenseUi = expense,
                    onEdit = {
                        /* idだけ渡してAdd/Editする */
                        onExpenseClick(expense.id)
                    },
                )
            }
        }
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
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

    Box(
        modifier = modifier
            .then(
                if (isToday) {
                    Modifier.border(1.dp, color = MaterialTheme.colorScheme.primary)
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
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "${day.date.dayOfMonth}",
                color = if (isCurrentMonth) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                },
            )
            /* when the value is too big, use the expression like 1.23K */
            Text(
                "¥${if (isCurrentMonth) price else 0}",
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(1.dp),
                fontSize = 12.sp
            )
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
            .height(40.dp)
            .border(
                width = 1.dp,
                color = if (isToday) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSecondary
            )
            .then(
                if (isToday) {
                    Modifier.background(
                        color = MaterialTheme.colorScheme.onTertiary,
                        shape = RectangleShape
                    )
                } else {
                    Modifier // ← ここを追加
                }
            )
            .clickable {
                onEdit()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${
                expenseUi.datetime?.dayOfMonth
            }日",
            modifier = Modifier.weight(1f),
            fontSize = 20.sp,
            textAlign = TextAlign.Left//左寄せ
        )
        Text(
            text = "${expenseUi.amount}円",
            modifier = Modifier.weight(1f),
            fontSize = 20.sp,
            textAlign = TextAlign.Left
        )
        Text(
            text = expenseUi.category?.name ?: "",
            modifier = Modifier.weight(1f),
            fontSize = 20.sp,
            textAlign = TextAlign.Left
        )
    }
}