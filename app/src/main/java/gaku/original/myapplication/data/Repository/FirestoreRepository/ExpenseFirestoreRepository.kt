package gaku.original.myapplication.data.Repository.FirestoreRepository

import addDataWithIdToFirestore
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.utility.LogClassFuncCalled
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import removeDataFromFirestore
import updateDataToFirestore
import java.time.YearMonth
import javax.inject.Inject

class ExpenseFirestoreRepository @Inject constructor(
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

    suspend fun fetchMonthsExpenses(
        fromMonth: YearMonth,
        toMonth: YearMonth,
        timeout: Long = 10000,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): FetchResult<List<Expense>> {
        val funcName = ::fetchMonthsExpenses.name
        LogClassFuncCalled(className, funcName)

        val expenseRef = getExpensesColRef()
        if (expenseRef == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Expensesコレクションが参照できませんでした"
            )
            callback(statusInfo)
            return FetchResult(statusInfo.status, statusInfo.errorMessage)
        }

        // ISO 8601 の文字列範囲を生成（UTCで扱う想定）
        val startDateTime =
            fromMonth.atDay(1).atStartOfDay().toString() + "Z" // "2025-03-01T00:00:00Z"
        val endDateTime =
            toMonth.plusMonths(1).atDay(1).atStartOfDay().toString() + "Z" // "2025-06-01T00:00:00Z"

        return try {
            withTimeout(timeout) {
                val snapshot = expenseRef
                    .whereGreaterThanOrEqualTo("datetime", startDateTime)
                    .whereLessThan("datetime", endDateTime)
                    .get()
                    .await()

                val list = snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }

                val statusInfo = SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, "")
                callback(statusInfo)
                FetchResult(statusInfo.status, statusInfo.errorMessage, list)
            }
        } catch (e: TimeoutCancellationException) {
            val statusInfo =
                SuspendFuncStatusInfo(SuspendFuncStatus.TIMEOUT, "タイムアウトしました")
            callback(statusInfo)
            FetchResult(statusInfo.status, statusInfo.errorMessage)
        } catch (e: Exception) {
            val statusInfo =
                SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, e.message ?: "不明なエラー")
            callback(statusInfo)
            FetchResult(statusInfo.status, statusInfo.errorMessage)
        }
    }


    suspend fun fetchAllExpenses(
        timeout: Long = 10000,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): FetchResult<List<Expense>> {
        val funcName = ::fetchAllExpenses.name
        LogClassFuncCalled(className, funcName)

        val expenseRef = getExpensesColRef()

        if (expenseRef == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Expensesコレクションが参照できませんでした"
            )
            callback(statusInfo)
            return FetchResult(
                statusInfo.status,
                statusInfo.errorMessage
            )
        }

        return try {
            withTimeout(timeout) {
                Log.d(className, "Start waiting for getUserExpenseRef.")
                val snapshot = expenseRef.get().await()

                val list = mutableListOf<Expense>()
                for (doc in snapshot.documents) {
                    val expense = doc.toObject(Expense::class.java)
                        ?: throw Exception("Expenseへの変換に失敗 docId=${doc.id}")
                    list.add(expense)
                }
                val statusInfo = SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, "")
                Log.d(className, "Fetched Expenses: $list")
                callback(statusInfo)

                /* 戻り値 */
                FetchResult(
                    statusInfo.status,
                    statusInfo.errorMessage,
                    list
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "$funcName Timeout.")
            val statusInfo =
                SuspendFuncStatusInfo(SuspendFuncStatus.TIMEOUT, "タイムアウトしました")
            callback(statusInfo)

            /* 戻り値 */
            FetchResult(
                statusInfo.status,
                statusInfo.errorMessage
            )
        } catch (e: Exception) {
            Log.d(className, "$funcName failed. ${e.message}")
            val statusInfo =
                SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, e.message ?: "不明なエラー")
            callback(statusInfo)

            /* 戻り値 */
            FetchResult(
                statusInfo.status,
                statusInfo.errorMessage
            )
        }
    }

}