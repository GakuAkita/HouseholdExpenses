package gaku.original.myapplication.data.Constants

object RepeatFrequency {
    const val EVERY_YEAR = "every_year"
    const val EVERY_MONTH = "every_month"
    const val WEEKDAY = "weekday"
    const val WEEKEND = "weekend"
    const val EVERYDAY = "everyday"
}

// `RepeatFrequency` の定数を配列に変換する関数
// リフレクションを使ってフィールド名を取得し、その値を変換
fun getRepeatFrequencyValues(): Array<String> {
    return RepeatFrequency::class.java.declaredFields
        .filter { field ->
            field.type == String::class.java  // 定数かつString型フィールドのみを対象にする
        }
        .map { field ->
            field.get(null) as String  // 値を取得してString型にキャスト
        }
        .toTypedArray()  // 配列に変換
}

val RepeatFrequencyArray = getRepeatFrequencyValues()
