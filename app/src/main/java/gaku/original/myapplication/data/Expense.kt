package gaku.original.myapplication.data

import java.time.LocalDateTime

data class Expense(
    val id: String,
    val datetime:LocalDateTime,
    val expense:Int,
    val category:String,
    val note:String
)

object DummyExpenses{
    val expensesList= listOf(
        Expense(
            id="1",
            datetime = LocalDateTime.of(2023,10,1,0,0,0),
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
        Expense(
            id="5",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
        Expense(
            id="6",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
        Expense(
            id="7",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
        Expense(
            id="8",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note=""
        ),
        Expense(
            id="9",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note=""
        ),
        Expense(
            id="9",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note=""
        ),
        Expense(
            id="10",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note=""
        ),
        Expense(
            id="11",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note=""
        ),
        Expense(
            id="12",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
        Expense(
            id="13",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
        Expense(
            id="14",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
        Expense(
            id="15",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
    )

}