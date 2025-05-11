package gaku.original.myapplication.data.FirestoreRepository

import com.google.firebase.firestore.DocumentReference
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.data.ErrorInfo

class ExpenseFirestoreRepository(
    private val firestoreReference: FirestoreReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    fun getExpensesColRef(callback: (ErrorInfo) -> Unit = {}): DocumentReference? {
        val ref = firestoreReference.getExpensesDocRef()
        if (ref == null) {
            callback(ErrorInfo(isError = true, errorMessage = "Expenses DocumentReference is null"))
        }
        return ref
    }
}