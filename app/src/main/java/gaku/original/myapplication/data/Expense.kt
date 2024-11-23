package gaku.original.myapplication.data

import androidx.compose.runtime.mutableStateListOf
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it, formatter) }
    }
}

@Entity(tableName="Expense-table")
data class Expense(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name="Expense-generated_type")
    val generatedType:String?,//自動生成なのか手動生成なのか
    @ColumnInfo(name="Expense-datetime")
    var datetime:LocalDateTime,
    @ColumnInfo(name="Expense-amount")
    var amount:Long?,
    @ColumnInfo(name="Expense-category")
    var category:String?,
    @ColumnInfo(name="Expense-note")
    var note:String?
)

data class CategoryClass(
    val id:String?,
    val name:String?
)

object DummyExpenses{
    val expensesList= mutableStateListOf(
        Expense(
            id="1",
            datetime = LocalDateTime.of(2023,10,1,0,0,0),
            amount=1000,
            category="necessities",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="2",
            datetime = LocalDateTime.now(),
            amount=120,
            category="waste",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="3",
            datetime = LocalDateTime.now(),
            amount=1500,
            category="Food",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="4",
            datetime = LocalDateTime.now(),
            amount=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="5",
            datetime = LocalDateTime.now(),
            amount=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="6",
            datetime = LocalDateTime.now(),
            amount=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="7",
            datetime = LocalDateTime.now(),
            amount=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="8",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            amount=1000,
            category="necessities",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="9",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            amount=1000,
            category="necessities",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="9.1",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            amount=1000,
            category="necessities",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="10",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            amount=1000,
            category="necessities",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="11",
            datetime = LocalDateTime.of(2024,10,1,0,0,0),
            amount=1000,
            category="necessities",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="12",
            datetime = LocalDateTime.now(),
            amount=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="13",
            datetime = LocalDateTime.now(),
            amount=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="14",
            datetime = LocalDateTime.now(),
            amount=2000,
            category="Amazon",
            note="",
            generatedType = "manual"
        ),
        Expense(
            id="15",
            datetime = LocalDateTime.now(),
            amount=2000,
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
        ),
        CategoryClass(
            id="4",
            name="Amazon2"
        ),
        CategoryClass(
            id="5",
            name="Amazon3"
        ),
        CategoryClass(
            id="6",
            name="Amazon"
        ),
        CategoryClass(
            id="7",
            name="Amazon5"
        ),
        CategoryClass(
            id="8",
            name="Amazon6"
        ),
        CategoryClass(
            id="9",
            name="Amazon7"
        )
    )
}