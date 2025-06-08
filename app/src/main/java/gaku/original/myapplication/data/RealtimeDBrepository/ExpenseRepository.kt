package gaku.original.myapplication.data.RealtimeDBrepository

import addDataToRTDb
import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.RealtimeDbReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.addSingleDataToRTDb
import gaku.original.myapplication.utility.LogClassFuncCalled
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import removeDataFromRTDb
import updateDataToRTDb

class ExpenseRepository(
    private val realtimeDbReference: RealtimeDbReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    suspend fun getExpenseRef(callback: (SuspendFuncStatus) -> Unit): DatabaseReference? {
        return realtimeDbReference.getUserExpenseRef(callback)
    }

    //SignUp後にやる操作
    suspend fun addUserInitialData(
        email: String,
        callback: (SuspendFuncStatus) -> Unit
    ): SuspendFuncStatus {
        val funcName: String = ::addUserInitialData.name
        LogClassFuncCalled(className, funcName)
        var ret = SuspendFuncStatus.FAILED

        val userRef = realtimeDbReference.getUserRef { status ->
            if (status != SuspendFuncStatus.SUCCESS) {
                callback(status) // 失敗時に早期リターン
            }
        }

        if (userRef == null) {
            return ret
        }

        ret = addSingleDataToRTDb(email, "email", userRef, callback = callback)

        return ret
    }


    // ユーザーIDに基づいてデータをリストとして返す（非同期）
    suspend fun fetchAllExpenses(
        callback: (SuspendFuncStatus) -> Unit
    ): List<Expense> {
        val funcName = ::fetchAllExpenses.name
        var ret = emptyList<Expense>()
        LogClassFuncCalled(className, funcName)

        /* まずはreferenceを取得 */
        val expenseRef = getExpenseRef { status ->
            if (status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        }

        if (expenseRef == null) {
            return ret
        }

        try {
            withTimeout(10000) {
                Log.d(className, "Start waiting for getUserExpenseRef.")
                val snapshot = expenseRef.get().await()
                val expenses = snapshot.children.mapNotNull {
                    it.getValue(Expense::class.java)
                }
                Log.d(className, "Fetched Expenses: $expenses")
                ret = expenses
                callback(SuspendFuncStatus.SUCCESS)
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "${funcName} Timeout.")
            callback(SuspendFuncStatus.TIMEOUT)
        } catch (e: Exception) {
            Log.d(className, "${funcName} failed. ${e.message}")
            callback(SuspendFuncStatus.FAILED)
        }
        return ret
    }

    suspend fun addExpense(
        expense: Expense,
        callback: (SuspendFuncStatus) -> Unit
    ): SuspendFuncStatus {
        var ret = SuspendFuncStatus.FAILED
        val funcName = ::addExpense.name
        LogClassFuncCalled(className, funcName)
        val reference = getExpenseRef { status ->
            if (status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        }

        /**
         * nullだったらタイムアウトかなにか事故ったということ
         * ここでreturnしておけばcallbackが二回実行されることはない
         */
        if (reference == null) {
            return ret
        }

        ret = addDataToRTDb(expense, reference) { status ->
            callback(status)
        }

        return ret
    }

    suspend fun updateExpense(
        expense: Expense,
        callback: (SuspendFuncStatus) -> Unit
    ): SuspendFuncStatus {
        val funcName = ::updateExpense.name
        LogClassFuncCalled(className, funcName)
        var ret = SuspendFuncStatus.FAILED
        val reference = getExpenseRef(callback = { status ->
            if (status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        })

        if (reference == null) {
            return ret
        }

        ret = updateDataToRTDb(expense, reference, callback = callback)

        return ret
    }

    suspend fun removeExpense(
        expense: Expense,
        callback: (SuspendFuncStatus) -> Unit
    ): SuspendFuncStatus {
        var ret = SuspendFuncStatus.FAILED
        val reference = getExpenseRef { status ->
            if (status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        }

        if (reference == null) {
            return ret
        }

        ret = removeDataFromRTDb(expense, reference, callback = callback)
        return ret
    }
}