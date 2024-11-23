package gaku.original.myapplication.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    suspend fun addAExpense(expense: Expense){
        expenseDao.addAExpense(expense)
    }

    fun getExpenseById(id:String): Flow<Expense> {
        return expenseDao.getExpenseById(id)
    }

    fun getExpensesByYearMonth(year: String, month: String): Flow<List<Expense>> {
        return expenseDao.getExpensesByYearMonth(year, month)
    }

    fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses()
    }

    suspend fun updateAExpense(expense: Expense){
        expenseDao.updateAExpense(expense)
    }

    suspend fun deleteAExpense(expense: Expense){
        expenseDao.deleteAExpense(expense)
    }
}