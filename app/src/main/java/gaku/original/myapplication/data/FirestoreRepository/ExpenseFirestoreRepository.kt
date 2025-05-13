package gaku.original.myapplication.data.FirestoreRepository

import addDataWithIdToFirestore
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.Utility.LogClassFuncCalled
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import removeDataFromFirestore
import updateDataToFirestore

class ExpenseFirestoreRepository(
    private val firestoreReference: FirestoreReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    fun getExpensesColRef(): CollectionReference? {
        return firestoreReference.getExpensesColRef()
    }

    suspend fun addExpense(
        expense: Expense,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val ref = getExpensesColRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Expensesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        /* タイムアウトは設定しない */
        val statusInfo = addDataWithIdToFirestore(expense, ref, callback = callback)
        return statusInfo
    }

    suspend fun updateExpense(
        expense: Expense,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val ref = getExpensesColRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Expensesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = updateDataToFirestore(expense, ref, callback = callback)
        return statusInfo
    }

    suspend fun removeExpense(
        expense: Expense,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val ref = getExpensesColRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Expensesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = removeDataFromFirestore(expense, ref, callback = callback)
        return statusInfo
    }

    suspend fun fetchAllExpenses(
        timeout: Long = 10000,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): List<Expense> {
        val funcName = ::fetchAllExpenses.name
        var ret = emptyList<Expense>()
        LogClassFuncCalled(className, funcName)

        val expenseRef = getExpensesColRef()

        if (expenseRef == null) {
            callback(
                SuspendFuncStatusInfo(
                    SuspendFuncStatus.FAILED,
                    "Expensesコレクションが参照できませんでした"
                )
            )
            return ret
        }

        try {
            withTimeout(timeout) {
                Log.d(className, "Start waiting for getUserExpenseRef.")
                val snapshot = expenseRef.get().await()

                val list = mutableListOf<Expense>()
                for (doc in snapshot.documents) {
                    val expense = doc.toObject(Expense::class.java)
                        ?: throw Exception("Expenseへの変換に失敗 docId=${doc.id}")
                    list.add(expense)
                }
                ret = list

                Log.d(className, "Fetched Expenses: $ret")
                callback(SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, ""))
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "$funcName Timeout.")
            val statusInfo =
                SuspendFuncStatusInfo(SuspendFuncStatus.TIMEOUT, "タイムアウトしました")
            callback(statusInfo)
            ret = emptyList()
        } catch (e: Exception) {
            Log.d(className, "$funcName failed. ${e.message}")
            val statusInfo =
                SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, e.message ?: "不明なエラー")
            callback(statusInfo)
            ret = emptyList()
        }

        return ret
    }

}