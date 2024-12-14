package gaku.original.myapplication.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.toLocalDateTime
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

//https://github.com/kizitonwose/Calendar
@Composable
fun CalendarDisplay(calendarYear:Int, calendarMonth:Int,monthExpenses:List<Expense>,onDayClicked: (day:CalendarDay) -> Unit={_-> }/* 引数ありで空関数のときはこの_を使った書き方らしい */) {
    // 現在の年月
    val calendarYearMonth = YearMonth.of(calendarYear,calendarMonth)
    // 現在より前の年月
    val startMonth =  calendarYearMonth.minusMonths(0)
    // 現在より後の年月
    val endMonth = calendarYearMonth.plusMonths(0)
    // 曜日
    val daysOfWeek =  daysOfWeek()
    // カレンダーの状態を持つ
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = calendarYearMonth,
        firstDayOfWeek = daysOfWeek.first(),
        outDateStyle = OutDateStyle.EndOfGrid
    )

    // 横スクロールのカレンダーを作成するためのComposable関数
    // 縦スクロールのVerticalなどもある
    HorizontalCalendar(
        state = state,
        // 日付を表示する部分
        dayContent = { Day(it,monthExpenses,onDayClicked) },
        // カレンダーのヘッダー
        monthHeader = { DaysOfWeekTitle(daysOfWeek = daysOfWeek) },
        //ユーザーのスクロール
        userScrollEnabled = false
    )
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
fun Day(day: CalendarDay,monthExpenses:List<Expense>,onClicked: (day:CalendarDay) -> Unit = { _ -> }) {
    // Calculate the total amount of expenses for this day
    val totalExpenseForDay = monthExpenses.filter { expense ->
        toLocalDateTime(expense.datetime)?.toLocalDate() == day.date // Filter expenses by matching the date
    }.mapNotNull { expense ->
        // Convert the expense amount to a numeric type, assuming it might be String or nullable.
        expense.amount
    }.sum()

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable {
                onClicked(day)
            }
        ,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Day number (e.g., 1, 2, 3, ...)
            Text(
                text = day.date.dayOfMonth.toString(),
                color = if (day.position == DayPosition.MonthDate) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodyLarge // Standard text style for the day
            )
            // Expense amount (money) with custom styling
            Text(
                text = "¥${totalExpenseForDay}", // Display the total expense amount
                color = MaterialTheme.colorScheme.primary, // Set text color
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}