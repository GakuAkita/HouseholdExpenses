package gaku.original.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ExpenseDao {
    @Insert(onConflict=OnConflictStrategy.ABORT)//同じのがあったときにどうするか
    abstract suspend  fun addExpense(ExpenseEntity: ExpenseClass)

    // Loads all Expenses from the Expense-table
    @Query("SELECT * FROM `Expense-table`")
    abstract fun getAllExpenses(): Flow<List<ExpenseClass>>//Flow使っている場合はsuspendはなくて良いらしい

    @Update
    abstract suspend fun updateAExpense(ExpenseEntity: ExpenseClass)

    @Delete
    abstract suspend fun deleteAExpense(ExpenseEntity: ExpenseClass)

    @Query("SELECT * FROM `Expense-table` WHERE id=:id")
    abstract fun getExpenseById(id:String):Flow<ExpenseClass>

    // Query expenses by year and month(GPTに作ってもらった)
    @Query("SELECT * FROM `Expense-table` WHERE strftime('%Y', `Expense-datetime`) = :year AND strftime('%m', `Expense-datetime`) = :month")
    abstract fun getExpensesByYearAndMonth(year: String, month: String): Flow<List<ExpenseClass>>
}