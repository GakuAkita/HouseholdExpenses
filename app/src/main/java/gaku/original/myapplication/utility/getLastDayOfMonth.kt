package gaku.original.myapplication.utility

import java.time.LocalDate
import java.time.temporal.TemporalAdjusters


// 月の最終日を取得
fun getLastDayOfMonth(year: Int, month: Int): LocalDate {
    return LocalDate.of(year, month, 1)
        .with(TemporalAdjusters.lastDayOfMonth())
}