package gaku.original.myapplication.data

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.database.ServerValue

data class Expense(
    var id: String?=null,//yyyy-mm-ddTHH:MM:SS-1
    var generatedType:String?=null,//自動生成なのか手動生成なのか
    var datetime:String?=null,//ISO_LOCAL_DATE_TIME
    var timestamp:Long? = System.currentTimeMillis(),
    var amount:Long?=null,
    var category:String?=null,
    var note:String?=null
)

data class CategoryClass(
    val name:String?
)