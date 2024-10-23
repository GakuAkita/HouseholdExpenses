package gaku.original.myapplication.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarDisplay(calendarYear:Int, calendarMonth:Int ) {
    // 現在の年月
    val calendarYearMonth = remember { YearMonth.of(calendarYear,calendarMonth) }
    // 現在より前の年月
    val startMonth = remember { calendarYearMonth.minusMonths(100) }
    // 現在より後の年月
    val endMonth = remember { calendarYearMonth.plusMonths(100) }
    // 曜日
    val daysOfWeek = remember { daysOfWeek() }
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
        dayContent = {Day(it)},
        // カレンダーのヘッダー
        monthHeader = {DaysOfWeekTitle(daysOfWeek = daysOfWeek)}
    )
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        for ((index, dayOfWeek) in daysOfWeek.withIndex()) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = "${index}" + dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                // 土日だけそれぞれ色を変えたいので対応したカラーコードを返している
//                color = getDayOfWeekTextColor(index)
            )
        }
    }
}

@Composable
fun Day(day: CalendarDay) {
    Box(
        modifier = Modifier
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            // ここで今月でないものの日付をグレーアウトさせている
            color = if (day.position == DayPosition.MonthDate) Color.Black else Color.Gray
        )
    }
}