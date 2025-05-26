package gaku.original.myapplication.data.Constants

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