package gaku.original.myapplication.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

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

    fun getExpensesByYearMonth(year: Int, month: Int): Flow<List<Expense>> {
        Log.d("Akita Debug","getExpensesByYearMonth(year:${year} month:${month}) was called")
        return expenseDao.getExpensesByYearMonth(year, month)
            .catch { //エラー処理
                e->
                Log.e("ExpenseRepository","Error fetching expenses:${e.message}")
                emit(emptyList())
            }
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