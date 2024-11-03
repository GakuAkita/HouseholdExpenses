package gaku.original.myapplication.data

import java.time.LocalDateTime

data class ExpenseClass(
    val id: String?,
    val datetime:LocalDateTime,
    val expense:Int?,
    val category:String?,
    val note:String?
)

object DummyExpenses{
    val expensesList= listOf(
        ExpenseClass(
            id="1",
            datetime = LocalDateTime.of(2023,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note=""
        ),
        ExpenseClass(
            id="2",
            datetime = LocalDateTime.now(),
            expense=120,
            category="waste",
            note=""
        ),
        ExpenseClass(
            id="3",
            datetime = LocalDateTime.now(),
            expense=1500,
            category="Food",
            note=""
        ),
        ExpenseClass(
            id="4",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
        ExpenseClass(
            id="5",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
        ExpenseClass(
            id="6",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
        ExpenseClass(
            id="7",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
        ExpenseClass(
            id="8",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note=""
        ),
        ExpenseClass(
            id="9",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note=""
        ),
        ExpenseClass(
            id="9",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note=""
        ),
        ExpenseClass(
            id="10",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note=""
        ),
        ExpenseClass(
            id="11",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            expense=1000,
            category="necessities",
            note=""
        ),
        ExpenseClass(
            id="12",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
        ExpenseClass(
            id="13",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
        ExpenseClass(
            id="14",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
        ExpenseClass(
            id="15",
            datetime = LocalDateTime.now(),
            expense=2000,
            category="Amazon",
            note=""
        ),
    )

}