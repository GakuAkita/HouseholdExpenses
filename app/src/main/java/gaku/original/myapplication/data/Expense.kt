package gaku.original.myapplication.data

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.database.ServerValue

data class Expense(
    var id: String?=null,//yyyy-mm-ddTHH:MM:SS-1
    var generatedType:String?=null,//自動生成なのか手動生成なのか
    var datetime:String?=null,//ISO_LOCAL_DATE_TIME
    var timestamp:Long? = System.currentTimeMillis(),
    var amount:Long?=null,
    var category:Category?=null,//ここCategoryのほうが良いのかな。idとnameを一緒に保存してしまう感じ
    var note:String?=null
)

data class Category(
    val id:String? = null,
    var timestamp:Long? = System.currentTimeMillis(),
    val name:String? = null,
    val enabled:Boolean? = true
)

class generatedType{
    companion object{
        const val AUTO = "auto"
        const val MANUAL = "manual"
    }
}

object DefaultCategories{
    //サインアップ時にデフォルトで登録されるカテゴリ
    val categories = mutableStateListOf(
        Category(name = "食費"),
        Category(name = "交通費"),
        Category(name = "日用品"),
        Category(name = "その他"),
    )
}