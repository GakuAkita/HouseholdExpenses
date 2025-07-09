package gaku.original.myapplication.data.Repository.RealtimeDBrepository

import addDataToRTDbWithPush
import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.RealtimeDbReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Repository.RealtimeDBrepository.RepositoryUtil.addSingleDataToRTDb
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Expense
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

    suspend fun getExpenseRef(callback: (SuspendFuncStatusInfo) -> Unit): DatabaseReference? {
        return realtimeDbReference.getUserExpenseRef(callback)
    }

    //SignUp後にやる操作
    suspend fun addUserInitialData(
        email: String,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val funcName: String = ::addUserInitialData.name
        LogClassFuncCalled(className, funcName)

        val userRef = realtimeDbReference.getUserRef { status ->
            if (status.status != SuspendFuncStatus.SUCCESS) {
                callback(status) // 失敗時に早期リターン
            }
        }

        if (userRef == null) {
            return SuspendFuncStatusInfo(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "userRefがnullでした"
            )
        }

        val ret = addSingleDataToRTDb(email, "email", userRef, callback = callback)

        return ret
    }


    // ユーザーIDに基づいてデータをリストとして返す（非同期）
    suspend fun fetchAllExpenses(
        callback: (SuspendFuncStatusInfo) -> Unit
    ): List<Expense> {
        val funcName = ::fetchAllExpenses.name
        var ret = emptyList<Expense>()
        LogClassFuncCalled(className, funcName)

        /* まずはreferenceを取得 */
        val expenseRef = getExpenseRef { status ->
            if (status.status != SuspendFuncStatus.SUCCESS) {
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
                val result = SuspendFuncStatusInfo(
                    status = SuspendFuncStatus.SUCCESS,
                    errorMessage = ""
                )
                callback(result)
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "${funcName} Timeout.")
            val result = SuspendFuncStatusInfo(
                status = SuspendFuncStatus.TIMEOUT,
                errorMessage = "タイムアウトしました"
            )
            callback(result)
        } catch (e: Exception) {
            Log.d(className, "${funcName} failed. ${e.message}")
            val result = SuspendFuncStatusInfo(
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "不明なエラー"
            )
            callback(result)
        }
        return ret
    }

    suspend fun addExpense(
        expense: Expense,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val funcName = ::addExpense.name
        LogClassFuncCalled(className, funcName)
        val reference = getExpenseRef { status ->
            if (status.status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        }

        /**
         * nullだったらタイムアウトかなにか事故ったということ
         * ここでreturnしておけばcallbackが二回実行されることはない
         */
        if (reference == null) {
            return SuspendFuncStatusInfo(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "referenceがnullでした"
            )
        }

        val ret = addDataToRTDbWithPush(expense, reference) { status ->
            callback(status)
        }

        return ret
    }

    suspend fun updateExpense(
        expense: Expense,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val funcName = ::updateExpense.name
        LogClassFuncCalled(className, funcName)
        val reference = getExpenseRef(callback = { status ->
            if (status.status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        })

        if (reference == null) {
            return SuspendFuncStatusInfo(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "referenceがnullでした"
            )
        }

        val ret = updateDataToRTDb(expense, reference, callback = callback)

        return ret
    }

    suspend fun removeExpense(
        expense: Expense,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val reference = getExpenseRef { status ->
            if (status.status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        }

        if (reference == null) {
            return SuspendFuncStatusInfo(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "referenceがnullでした"
            )
        }

        val ret = removeDataFromRTDb(expense, reference, callback = callback)
        return ret
    }
}