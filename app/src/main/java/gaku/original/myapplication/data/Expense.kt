package gaku.original.myapplication.data

data class Expense(
    var id: String?,//yyyy-mm-ddTHH:MM:SS-1
    val generatedType:String?,//自動生成なのか手動生成なのか
    var datetime:String?,//ISO_LOCAL_DATE_TIME
    var amount:Long?,
    var category:String?,
    var note:String?
)

data class CategoryClass(
    val name:String?
)
