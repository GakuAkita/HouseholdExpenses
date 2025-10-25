package gaku.original.myapplication.repository.FirestoreRepository

import addDataWithIdToFirestore
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.utility.LogClassFuncCalled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
    ): FuncResultWithData<Expense> {
        val ref = getExpensesColRef()
        if (ref == null) {
            val statusInfo = FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "Expensesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        /* タイムアウトは設定しない */
        val statusInfo = addDataWithIdToFirestore(expense, ref)
        return statusInfo
    }

    suspend fun updateExpense(
        expense: Expense
    ): FuncStatusInfo {
        val ref = getExpensesColRef()
        if (ref == null) {
            val statusInfo = FuncStatusInfo(
                FuncStatus.FAILED,
                "Expensesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = updateDataToFirestore(expense, ref)
        return statusInfo
    }

    suspend fun removeExpense(
        expense: Expense,
    ): FuncStatusInfo {
        val ref = getExpensesColRef()
        if (ref == null) {
            val statusInfo = FuncStatusInfo(
                FuncStatus.FAILED,
                "Expensesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = removeDataFromFirestore(expense, ref)
        return statusInfo
    }

    suspend fun fetchMonthsExpenses(
        fromMonth: YearMonth,
        toMonth: YearMonth,
        timeout: Long = 10000
    ): FuncResultWithData<List<Expense>> {
        val funcName = ::fetchMonthsExpenses.name
        LogClassFuncCalled(className, funcName)

        val expenseRef = getExpensesColRef()
        if (expenseRef == null) {
            val result = FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "Expensesコレクションが参照できませんでした"
            )
            return result
        }

        // ISO 8601 の文字列範囲を生成（UTCで扱う想定）
        val startDateTime =
            fromMonth.atDay(1).atStartOfDay().toString() + "Z" // "2025-03-01T00:00:00Z"
        val endDateTime =
            toMonth.plusMonths(1).atDay(1).atStartOfDay().toString() + "Z" // "2025-06-01T00:00:00Z"

        return try {
            withTimeout(timeout) {
                withContext(Dispatchers.IO) {/* これをしないとメインスレッドを止めてしまう？ */
                    val snapshot = expenseRef
                        .whereGreaterThanOrEqualTo("datetime", startDateTime)
                        .whereLessThan("datetime", endDateTime)
                        .get()
                        .await()

                    val list = snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
                    val result = FuncResultWithData.Success(
                        data = list
                    )
                    result
                }
            }
        } catch (e: TimeoutCancellationException) {
            val result = FuncResultWithData.Failure.Timeout()
            result
        } catch (e: Exception) {
            val result = FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = e.message ?: "不明なエラー"
            )
            result
        }
    }

    suspend fun fetchNotCategorizedExpenses(
        timeout: Long = 10000
    ): FuncResultWithData<List<Expense>> {
        val funcName = ::fetchNotCategorizedExpenses.name
        LogClassFuncCalled(className, funcName)

        val expenseRef = getExpensesColRef()
            ?: return FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "Expensesコレクションが参照できませんでした"
            )

        return try {
            withTimeout(timeout) {
                withContext(Dispatchers.IO) {
                    /**
                     * 注意:whereEqualTo(...,null)は、フィールドが存在しないものは取得できない
                     * ちゃんとnullという値が入っていないと
                     */
                    val snapshot = expenseRef
                        .whereEqualTo("category", null)
                        .get()
                        .await()
                    val list = snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }

                    FuncResultWithData.Success(data = list)
                }
            }
        } catch (e: TimeoutCancellationException) {
            FuncResultWithData.Failure.Timeout()
        } catch (e: Exception) {
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = e.message ?: "不明なエラー"
            )
        }
    }


    suspend fun fetchAllExpenses(
        timeout: Long = 10000
    ): FuncResultWithData<List<Expense>> {
        val funcName = ::fetchAllExpenses.name
        LogClassFuncCalled(className, funcName)

        val expenseRef = getExpensesColRef()

        if (expenseRef == null) {
            val result = FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "Expensesコレクションが参照できませんでした"
            )
            return result
        }

        return try {
            withTimeout(timeout) {
                withContext(Dispatchers.IO) {
                    Log.d(className, "Start waiting for getUserExpenseRef.")
                    val snapshot = expenseRef.get().await()

                    val list = mutableListOf<Expense>()
                    for (doc in snapshot.documents) {
                        val expense = doc.toObject(Expense::class.java)
                            ?: throw Exception("Expenseへの変換に失敗 docId=${doc.id}")
                        list.add(expense)
                    }

                    val result = FuncResultWithData.Success(list)
                    Log.d(className, "Fetched Expenses: $list")
                    /* 戻り値 */
                    result
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "$funcName Timeout.")
            val result = FuncResultWithData.Failure.Timeout()
            /* 戻り値 */
            result
        } catch (e: Exception) {
            Log.d(className, "$funcName failed. ${e.message}")
            val result = FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = e.message ?: "不明なエラー"
            )
            /* 戻り値 */
            result
        }
    }
}