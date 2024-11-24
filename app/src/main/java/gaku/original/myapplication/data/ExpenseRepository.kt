package gaku.original.myapplication.data

import android.util.Log
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao):idGeneration {
    //これでいいのかわからんが、id生成類はinterfaceにまとめておく
    suspend fun addAExpense(expense: Expense, num: Int){
        //
        if(expense.id==""){
            expense.id=generateExpenseId(num)
            expenseDao.addAExpense(expense)
        }else{
            Log.d("Akita Debug","addAExpense was called, but id is not \"\"")
        }
    }

    fun getExpenseById(id:String): Flow<Expense> {
        return expenseDao.getExpenseById(id)
    }

    fun getExpensesByYearMonth(year: String, month: String): Flow<List<Expense>> {
        Log.d("Akita Debug","getExpensesByYearMonth was called")
        return expenseDao.getExpensesByYearMonth(year, month)
    }

    fun getAllExpenses(): Flow<List<Expense>> {
        Log.d("Akita Debug","getAllExpenses was called")
        return expenseDao.getAllExpenses()
    }

    suspend fun updateAExpense(expense: Expense){
        expenseDao.updateAExpense(expense)
    }

    suspend fun deleteAExpense(expense: Expense){
        expenseDao.deleteAExpense(expense)
    }
}