package gaku.original.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Dao
interface ExpenseDao{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAExpense(EXpenseEntity:Expense)

    @Update
    suspend fun updateAExpense(ExpenseEntity:Expense)

    @Delete
    suspend fun deleteAExpense(ExpenseEntity: Expense)

    @Query("SELECT * FROM `Expense-table` WHERE id=:id")
    fun getExpenseById(id:String): Flow<Expense>

    // Query expenses by year and month(GPTに作ってもらった)
    @Query("SELECT * FROM `Expense-table` WHERE strftime('%Y', `Expense-datetime`) = :year AND strftime('%m', `Expense-datetime`) = :month")
    fun getExpensesByYearMonth(year: String, month: String): Flow<List<Expense>>

    @Query("SELECT * FROM `Expense-table`")
    fun getAllExpenses(): Flow<List<Expense>>
}