package gaku.original.myapplication.data

import androidx.compose.runtime.mutableStateListOf
import java.time.LocalDateTime

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

object DummyExpenses{
    val expensesList= mutableStateListOf(
        Expense(
            id="1",
            datetime = "20241202T12:34:56",
            amount=1000,
            category="necessities",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="2",
            datetime = "20241203T12:34:56",
            amount=120,
            category="waste",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="3",
            datetime = "20241204T12:34:56",
            amount=1500,
            category="Food",
            note="",
            generatedType = "manual"
        )
    )
}