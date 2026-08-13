package gaku.original.myapplication.data.Constants

import gaku.original.myapplication.data.dataClass.Frequency
import java.time.LocalDateTime

object RepeatFrequency {
    const val EVERY_YEAR = "every_year"
    const val EVERY_MONTH = "every_month"
    const val EVERY_WEEK = "every_week"
    const val WEEKDAYS = "weekdays"
    const val WEEKENDS = "weekends"
    const val EVERYDAY = "everyday"
}

// 定数を手動で配列に格納して順番を保証
/** 上で何かを追加したときは、こっちにも追加しないとだめ！！ **/
fun getRepeatFrequencyValues(): Array<String> {
    return arrayOf(
        RepeatFrequency.EVERY_YEAR,
        RepeatFrequency.EVERY_MONTH,
        RepeatFrequency.EVERY_WEEK,
        RepeatFrequency.WEEKDAYS,
        RepeatFrequency.WEEKENDS,
        RepeatFrequency.EVERYDAY
    )
}

/* dayOfWeek.valueで数値、dayOfWeek.labelで文字列がでる */
enum class DayOfWeek(val value: Int, val label: String) {
    SUN(0, "Sunday"),
    MON(1, "Monday"),
    TUE(2, "Tuesday"),
    WED(3, "Wednesday"),
    THU(4, "Thursday"),
    FRI(5, "Friday"),
    SAT(6, "Saturday");

    companion object {
        fun fromValue(value: Int): DayOfWeek? = entries.find { it.value == value }

        fun getLabels(): List<String> = entries.map { it.label }
    }
}

/**
 * RepeatAddの設定値を今のタイムゾーンとして日時を返す
 * 受け取った側でUTCに変換する
 */
fun getDaysInMonthByFrequency(
    frequencySetting: Frequency,
    baseTime: LocalDateTime = LocalDateTime.now()
): List<LocalDateTime> {
    val year = baseTime.year
    val month = baseTime.month
    val daysInMonth = month.length(baseTime.toLocalDate().isLeapYear)
    val result = mutableListOf<LocalDateTime>()

    when (frequencySetting.frequency) {
        RepeatFrequency.EVERY_YEAR -> {
            // 年に1回、指定の月日が今月なら返す
            if (frequencySetting.month == month.value && frequencySetting.day != null) {
                result.add(
                    LocalDateTime.of(
                        year,
                        month,
                        frequencySetting.day,
                        frequencySetting.hour ?: 0,
                        frequencySetting.minute ?: 0
                    )
                )
            }
        }

        RepeatFrequency.EVERY_MONTH -> {
            // 月に1回、指定日
            frequencySetting.day?.let { day ->
                if (day in 1..daysInMonth) {
                    result.add(
                        LocalDateTime.of(
                            year,
                            month,
                            day,
                            frequencySetting.hour ?: 0,
                            frequencySetting.minute ?: 0
                        )
                    )
                }
            }
        }

        RepeatFrequency.EVERY_WEEK -> {
            // 週ごとに指定曜日（複数可）
            frequencySetting.dayOfWeek?.forEach { dow ->
                for (day in 1..daysInMonth) {
                    val date = LocalDateTime.of(
                        year,
                        month,
                        day,
                        frequencySetting.hour ?: 0,
                        frequencySetting.minute ?: 0
                    )
                    if (date.dayOfWeek.value % 7 == dow) { // 0:日〜6:土 の対応
                        result.add(date)
                    }
                }
            }
        }

        RepeatFrequency.WEEKDAYS -> {
            // 月〜金
            for (day in 1..daysInMonth) {
                val date = LocalDateTime.of(
                    year,
                    month,
                    day,
                    frequencySetting.hour ?: 0,
                    frequencySetting.minute ?: 0
                )
                if (date.dayOfWeek.value in 1..5) {
                    result.add(date)
                }
            }
        }

        RepeatFrequency.WEEKENDS -> {
            // 土日
            for (day in 1..daysInMonth) {
                val date = LocalDateTime.of(
                    year,
                    month,
                    day,
                    frequencySetting.hour ?: 0,
                    frequencySetting.minute ?: 0
                )
                if (date.dayOfWeek.value == DayOfWeek.SAT.value || date.dayOfWeek.value == DayOfWeek.SUN.value) {
                    result.add(date)
                }
            }
        }

        RepeatFrequency.EVERYDAY -> {
            // 全日
            for (day in 1..daysInMonth) {
                result.add(
                    LocalDateTime.of(
                        year,
                        month,
                        day,
                        frequencySetting.hour ?: 0,
                        frequencySetting.minute ?: 0
                    )
                )
            }
        }

        else -> { /* 未設定は空リスト */
        }
    }

    return result
}
