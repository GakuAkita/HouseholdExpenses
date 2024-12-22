package gaku.original.myapplication.data

import android.util.Log
import gaku.original.myapplication.RealtimeDbReference
import kotlinx.coroutines.tasks.await

class ExpenseRepository(
    private val realtimeDbReference: RealtimeDbReference
) {
    //SignUp後にやる操作
    fun addUserInitialData(email: String) {
        val userRef = realtimeDbReference.getUserRef()
        userRef.child("email").setValue(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ExpenseRepository", "addUserInitialData successful")
                } else {
                    Log.e("ExpenseRepository", "Failed to add initialData", task.exception)
                }
            }
    }

    // ユーザーIDに基づいてデータをリストとして返す（非同期）
    suspend fun fetchUserExpenses(
        callback: (Boolean) -> Unit = {}
    ): List<Expense> {
        try {
            val snapshot = realtimeDbReference.getUserExpenseRef().get().await()
            val expenses = snapshot.children.mapNotNull {
                it.getValue(Expense::class.java)
            }
            Log.d("ExpenseRepository", "Fetched Expenses: $expenses")
            callback(true)
            return expenses
        } catch (e: Exception) {
            Log.d("ExpenseRepository", "fetchUserExpenses failed. ${e.message}")
            callback(false)
            return emptyList()  // エラー時には空のリストを返す
        }
    }

    fun addExpense(
        expense: Expense,
        callback: (Boolean) -> Unit = {}
    ) {
        val expenseRef = realtimeDbReference.getUserExpenseRef()
        val newExpenseRef = expenseRef.push() // Generate the unique key

        // Create a new instance of Expense with the generated ID
        val expenseWithId = expense.copy(id = newExpenseRef.key)

        // Save the new instance with the generated key
        newExpenseRef.setValue(expenseWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ExpenseRepository", "Expense added successfully")
                    callback(true)
                } else {
                    Log.e("ExpenseRepository", "Failed to add expense", task.exception)
                    callback(false)
                }
            }
    }

    fun updateExpense(
        expense: Expense,
        callback: (Boolean) -> Unit = {}
    ) {
        val expenseRef = realtimeDbReference.getUserExpenseRef()

        // Use the expense's ID (which is the Firebase-generated key) to locate it
        val expenseToUpdateRef = expenseRef.child(expense.id ?: return)

        expenseToUpdateRef.setValue(expense)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ExpenseRepository", "Expense updated successfully")
                    callback(true)
                } else {
                    Log.e("ExpenseRepository", "Failed to update expense", task.exception)
                    callback(false)
                }
            }
    }

    fun removeExpense(
        expense:Expense,
        callback: (Boolean) -> Unit = {}
    ){
        val expenseRef = realtimeDbReference.getUserExpenseRef()
        val expenseToRemoveRef = expenseRef.child(expense.id ?: return)
        expenseToRemoveRef.removeValue()
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Log.d("ExpenseRepository","Expense removed successfully")
                    callback(true)
                } else{
                    Log.e("ExpenseRepository","Failed to remove expense",task.exception)
                    callback(false)
                }
            }
    }
}
