package gaku.original.myapplication.data.FirestoreRepository

import addDataWithIdToFirestore
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.Utility.LogClassFuncCalled
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.ExpenseFetchResult
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.setDataToFirestore
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import removeDataFromFirestore
import updateDataToFirestore

class ExpenseFirestoreRepository(
    private val firebaseAuth: FirebaseAuth,/* まあ、FirestoreReference内で持っているけどこっちでも持っている */
    private val firestoreReference: FirestoreReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    fun getExpensesColRef(): CollectionReference? {
        return firestoreReference.getExpensesColRef()
    }

    //SignUp後にやる操作
    suspend fun addUserInitialData(
        email: String,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val funcName: String = ::addUserInitialData.name
        LogClassFuncCalled(className, funcName)

        val userRef = firestoreReference.getUserDocRef()
        if (userRef == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "ユーザーIDが空でユーザーDocを取得できませんでした。"
            )
            callback(statusInfo)
            return statusInfo
        }

        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "ユーザーIDが空です。"
            )
            callback(statusInfo)
            return statusInfo
        }
        val newMap = mapOf("email" to email, "id" to uid)

        val statusInfo = setDataToFirestore(newMap, reference = userRef, callback = callback)
        return statusInfo
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
    ): ExpenseFetchResult {
        val funcName = ::fetchAllExpenses.name
        LogClassFuncCalled(className, funcName)

        val expenseRef = getExpensesColRef()

        if (expenseRef == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Expensesコレクションが参照できませんでした"
            )
            callback(statusInfo)
            return ExpenseFetchResult(
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
                ExpenseFetchResult(
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
            ExpenseFetchResult(
                statusInfo.status,
                statusInfo.errorMessage
            )
        } catch (e: Exception) {
            Log.d(className, "$funcName failed. ${e.message}")
            val statusInfo =
                SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, e.message ?: "不明なエラー")
            callback(statusInfo)

            /* 戻り値 */
            ExpenseFetchResult(
                statusInfo.status,
                statusInfo.errorMessage
            )
        }
    }

}