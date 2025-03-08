package gaku.original.myapplication.data

import android.util.Log
import gaku.original.myapplication.RealtimeDbReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.RepositoryUtil.addDataToRTDb
import gaku.original.myapplication.data.RepositoryUtil.removeDataFromRTDb
import gaku.original.myapplication.data.RepositoryUtil.updateDataToRTDb
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class ExpenseRepository(
    private val realtimeDbReference: RealtimeDbReference
) {
    suspend fun get

    //SignUp後にやる操作
    suspend fun addUserInitialData(email: String) {
        val userRef = realtimeDbReference.getUserRef()
        userRef?.let {
            userRef.child("email").setValue(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //ユーザーにはinitial dataを追加したかはわからなくていいか。
                        Log.d("ExpenseRepository", "addUserInitialData successful")
                    } else {
                        Log.e("ExpenseRepository", "Failed to add initialData", task.exception)
                    }
                }
        }
    }

    // ユーザーIDに基づいてデータをリストとして返す（非同期）
    suspend fun fetchUserExpenses(
        callback: (SuspendFuncStatus) -> Unit = {}
    ): List<Expense> {
        var ret = emptyList<Expense>()
        Log.d("ExpenseRepository", "fetchUserExpenses was called.")
        try {
            withTimeout(2000) {
                //オフラインのとき、getUserExpenseRefでずっと待ってしまっている
                Log.d("ExpenseRepository", "Start waiting for getUserExpenseRef.")
                val snapshot = realtimeDbReference.getUserExpenseRef().get().await()
                Log.d("ExpenseRepository", "getUserExpenseRef finished.")
                val expenses = snapshot.children.mapNotNull {
                    it.getValue(Expense::class.java)
                }
                Log.d("ExpenseRepository", "Fetched Expenses: $expenses")
                callback(SuspendFuncStatus.SUCCESS)
                ret = expenses
            }
        } catch (e: Exception) {
            Log.d("ExpenseRepository", "fetchUserExpenses Timeout.")
            callback(SuspendFuncStatus.TIMEOUT)
        } catch (e: Exception) {
            Log.d("ExpenseRepository", "fetchUserExpenses failed. ${e.message}")
            callback(SuspendFuncStatus.FAILED)
        }
        return ret
    }

    fun addExpense(
        expense: Expense,
        callback: (Boolean) -> Unit = {}
    ) {
        addDataToRTDb(expense, { expenseRef }, callback)
    }

    fun updateExpense(
        expense: Expense,
        callback: (Boolean) -> Unit = {}
    ) {
        updateDataToRTDb(expense, { expenseRef }, callback)
    }

    fun removeExpense(
        expense: Expense,
        callback: (Boolean) -> Unit = {}
    ) {
        removeDataFromRTDb(expense, { expenseRef }, callback)
    }
}
