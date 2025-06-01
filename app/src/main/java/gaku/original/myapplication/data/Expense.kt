package gaku.original.myapplication.data

import androidx.compose.runtime.mutableStateListOf
import gaku.original.myapplication.Utility.AppTimeZone
import gaku.original.myapplication.data.Interface.CommonProperty

data class Expense(
    override var id: String? = null,//yyyy-mm-ddTHH:MM:SS-1
    var generatedType: String? = null,//自動生成なのか手動生成なのか
    var datetime: String? = null,//ISO_LOCAL_DATE_TIME
    override var timestamp: Long? = System.currentTimeMillis(),
    var amount: Long? = null,
    var category: Category? = null,//ここCategoryのほうが良いのかな。idとnameを一緒に保存してしまう感じ
    var note: String? = null
) : CommonProperty

data class Category(
    override var id: String? = null,
    override var timestamp: Long? = System.currentTimeMillis(),
    val name: String? = null,
    val enabled: Boolean? = true
) : CommonProperty


val defaultCategory = Category(
    id = null,
    timestamp = System.currentTimeMillis(),
    name = null,
    enabled = true
)

val defaultExpense = Expense(
    id = null,
    datetime = AppTimeZone.getCurrentTimeInUTCString(),
    amount = null,
    category = null,
    note = null,
    generatedType = null
)

/* 使われていない？↓ */
/* firebase functions側と一致させないとまずい */
class generatedType {
    companion object {
        const val AUTO = "auto"
        const val MANUAL = "manual"
        const val REPEAT_ADD = "repeat_add" // 繰り返し追加で追加するやつ
    }
}

object InitialCategories {
    //サインアップ時にデフォルトで登録されるカテゴリ
    val categories = mutableStateListOf(
        Category(name = "食費"),
        Category(name = "交通費"),
        Category(name = "生活費"),
        Category(name = "電気代"),
        Category(name = "水道代"),
        Category(name = "その他"),
    )
}