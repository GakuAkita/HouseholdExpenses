package gaku.original.myapplication.data

import androidx.compose.runtime.mutableStateListOf
import java.time.LocalDateTime

data class ExpenseClass(
    val id: String?,
    val generatedType:String?,//自動生成なのか手動生成なのか
    var datetime:LocalDateTime,
    var expense:Long?,
    var category:String?,
    var note:String?
)

data class CategoryClass(
    val id:String?,
    val name:String?
)

object DummyExpenses{
    val expensesList= mutableStateListOf(
        ExpenseClass(
            id="1",
            datetime = LocalDateTime.of(2023,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="2",
            datetime = LocalDateTime.now(),
            expense=120,
            category="waste",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="3",
            datetime = LocalDateTime.now(),
            expense=1500,
            category="Food",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="4",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="5",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="6",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="7",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="8",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="9",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="9",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="10",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="11",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="12",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="13",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="14",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
        ExpenseClass(
            id="15",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
    )
}

object DummyCategory{
    val categoryList= mutableListOf(
        CategoryClass(
            id="1",
            name="necessities"
        ),
        CategoryClass(
            id="2",
            name="waste"
        ),
        CategoryClass(
            id="3",
            name="Amazon"
        )
    )
}