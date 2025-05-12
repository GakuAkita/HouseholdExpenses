package gaku.original.myapplication.data.FirestoreRepository

import addDataWithIdToFirestore
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.SuspendFuncStatusInfo
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
}