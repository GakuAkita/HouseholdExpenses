package gaku.original.myapplication.data

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime

data class Expense(
    val id: String,
    val datetime:LocalDateTime,
    val expense:Int,
    val category:String,
    val note:String
)

object DummyExpenses{
    @RequiresApi(Build.VERSION_CODES.O)
    val expensesList= listOf(
        Expense(
            id="1",
            datetime = LocalDateTime.now(),
            expense=1000,
            category="necessities",
            note=""
        ),
        Expense(
            id="2",
            datetime = LocalDateTime.now(),
            expense=120,
            category="waste",
            note=""
        ),
        Expense(
            id="3",
            datetime = LocalDateTime.now(),
            expense=1500,
            category="Food",
            note=""
        ),
        Expense(
            id="4",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
    )

}