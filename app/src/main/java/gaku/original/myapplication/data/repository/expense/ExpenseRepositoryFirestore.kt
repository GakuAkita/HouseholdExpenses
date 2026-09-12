package gaku.original.myapplication.data.repository.expense

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.domain.AppUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class ExpenseRepositoryFirestore(
    appUser: AppUser,
    firestore: FirebaseFirestore
) : ExpenseRepository {
    private val expenseCollection = firestore.collection("users").document(appUser.id!!).collection(
        "expenses"
    )

    private val _expenses = MutableStateFlow<Map<String, Expense>>(emptyMap())
    override val expenses: StateFlow<Map<String, Expense>>
        get() = _expenses

    override fun startListening(query: ExpenseQuery) {
        var firestoreQuery: Query = expenseCollection

        query.datetimeFromOrEqual?.let {
            firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo(
                "datetime",
                it
            )
        }

        query.datetimeTo?.let {
            firestoreQuery = firestoreQuery.whereLessThan(
                "datetime",
                it
            )
        }

        firestoreQuery.addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                Timber.d("Error: $exception")
                throw Exception(exception)
            }

            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        val expense = dc.document.toObject(Expense::class.java)
                        _expenses.value = _expenses.value + (expense.id!! to expense)
                    }

                    DocumentChange.Type.MODIFIED -> {
                        val expense = dc.document.toObject(Expense::class.java)
                        _expenses.value = _expenses.value + (expense.id!! to expense)
                    }

                    DocumentChange.Type.REMOVED -> {
                        val expense = dc.document.toObject(Expense::class.java)
                        _expenses.value = _expenses.value - expense.id!!
                    }
                }
            }
        }
    }

    override fun stopListening() {
        _expenses.value = emptyMap()

    }

    override suspend fun addExpense(expense: Expense): Expense {
        return Expense()
    }

    override suspend fun updateExpense(expense: Expense): Expense {
        return Expense()
    }

    override suspend fun removeExpense(id: String) {
    }
}