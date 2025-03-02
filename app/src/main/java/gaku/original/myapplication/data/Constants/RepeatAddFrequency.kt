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