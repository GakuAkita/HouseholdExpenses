package gaku.original.myapplication.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

//https://github.com/kizitonwose/Calendar
@Composable
fun CalendarDisplay(calendarYear:Int, calendarMonth:Int ) {
    // 現在の年月
    val calendarYearMonth = YearMonth.of(calendarYear,calendarMonth)
    // 現在より前の年月
    val startMonth =  calendarYearMonth.minusMonths(10)
    // 現在より後の年月
    val endMonth = calendarYearMonth.plusMonths(10)
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
        dayContent = {Day(it)},
        // カレンダーのヘッダー
        monthHeader = {DaysOfWeekTitle(daysOfWeek = daysOfWeek)},
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