package gaku.original.myapplication.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val ExpenseDao: ExpenseDao) {
    suspend fun addExpense(Expense: Expense) {
        ExpenseDao.addExpense(Expense)
    }

    fun getExpenses(): Flow<List<Expense>> = ExpenseDao.getAllExpenses()

    fun getAExpenseById(id:String):Flow<Expense>{
        return ExpenseDao.getExpenseById(id)
    }

    fun getMonthExpenses(year: String, month: String): Flow<List<Expense>> {
        return ExpenseDao.getExpensesByYearAndMonth(year, month)
    }

    suspend fun updateAExpense(Expense: Expense){
        ExpenseDao.updateAExpense(Expense)
    }

    suspend fun deleteAExpense(Expense: Expense){
        ExpenseDao.deleteAExpense(Expense)
    }
}