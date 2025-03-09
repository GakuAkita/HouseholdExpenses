package gaku.original.myapplication.data

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.RealtimeDbReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.RepositoryUtil.addDataToRTDb
import gaku.original.myapplication.data.RepositoryUtil.removeDataFromRTDb
import gaku.original.myapplication.data.RepositoryUtil.updateDataToRTDb
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class ExpenseRepository(
    private val realtimeDbReference: RealtimeDbReference
) {

    suspend fun getExpenseRef(callback: (SuspendFuncStatus) -> Unit = {}): DatabaseReference? {
        return realtimeDbReference.getUserExpenseRef(callback)
    }

    //SignUp後にやる操作
    suspend fun addUserInitialData(email: String, callback: (SuspendFuncStatus) -> Unit) {
        val userRef = realtimeDbReference.getUserRef { status ->
            if (status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        }
        //userRefがnullの場合は、callback(FAILED)はgetUserRefの中で実行されている
        userRef?.let { ref ->/* userRefのこと */
            try {
                withTimeout(2000) {
                    ref.child("email").setValue(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //ユーザーにはinitial dataを追加したかはわからなくていいか。
                                Log.d("ExpenseRepository", "addUserInitialData successful")
                                callback(SuspendFuncStatus.SUCCESS)
                            } else {
                                Log.e(
                                    "ExpenseRepository",
                                    "Failed to add initialData",
                                    task.exception
                                )
                                throw Exception("Failed to add initialData")
                            }
                        }
                }
            } catch (e: TimeoutCancellationException) {
                callback(SuspendFuncStatus.TIMEOUT)
            } catch (e: Exception) {
                callback(SuspendFuncStatus.FAILED)
            }
        } ?: run {
            /* nullの場合はスルー */
            return
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

suspend fun addExpense(
    expense: Expense,
    callback: (SuspendFuncStatus) -> Unit = {}
) {

    val ref = getExpenseRef { status ->

    }

    /**
     * nullだったらタイムアウトかなにか事故ったということ
     * ここでreturnしておけばcallbackが二回実行されることはない
     */
    if (ref == null) {
        return
    }

    addDataToRTDb(expense, ref, callback = callback)
}

suspend fun updateExpense(
    expense: Expense,
    callback: (SuspendFuncStatus) -> Unit = {}
) {
    val ref = getExpenseRef(callback = { status ->
        if (status == SuspendFuncStatus.SUCCESS) {
            /* Do nothing */
        } else if (status == SuspendFuncStatus.TIMEOUT) {
            callback(SuspendFuncStatus.TIMEOUT)
        } else {
            callback(SuspendFuncStatus.FAILED)
        }
    })

    if (ref == null) {
        return
    }

    updateDataToRTDb(expense, ref, callback = callback)
}

suspend fun removeExpense(
    expense: Expense,
    callback: (SuspendFuncStatus) -> Unit = {}
) {
    val ref = async {
        getExpenseRef(callback = { status ->
            if (status == SuspendFuncStatus.SUCCESS) {
                /* Do nothing */
            } else if (status == SuspendFuncStatus.TIMEOUT) {
                callback(SuspendFuncStatus.TIMEOUT)
            } else {
                callback(SuspendFuncStatus.FAILED)
            }
        }).await()
    }

    //これすぐ止まっちゃう？
    if (ref == null) {
        return
    }

    removeDataFromRTDb(expense, ref, callback)
}
