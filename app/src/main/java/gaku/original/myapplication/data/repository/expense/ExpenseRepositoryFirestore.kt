package gaku.original.myapplication.data.repository.expense

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.domain.AppUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

class ExpenseRepositoryFirestore(
    appUser: AppUser, firestore: FirebaseFirestore
) : ExpenseRepository {
    private val expenseCollection = firestore.collection("users").document(appUser.id!!).collection(
        "expenses"
    )

    /* key is subscription id. */
    private val listeners = mutableMapOf<String, ListenerRegistration>()

    private val _expenses = MutableStateFlow<Map<String, Map<String, Expense>>>(emptyMap())
    override val expenses: StateFlow<Map<String, Map<String, Expense>>>
        get() = _expenses

    override fun startListening(subscriptionId: String, query: ExpenseQuery) {
        // すでにリスナーがある場合は何もしない（あるいは再起動するかは要件次第だが、一旦重複回避）
        if (listeners.containsKey(subscriptionId)) return
        var firestoreQuery: Query = expenseCollection
//        query.datetimeFromOrEqual?.let {
//            Timber.d("datetimeFromOrEqual=$it")
//            firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo(
//                "datetime", it
//            )
//        }
//
//        query.datetimeTo?.let {
//            Timber.d("datetimeTo=$it")
//            firestoreQuery = firestoreQuery.whereLessThan(
//                "datetime", it
//            )
//        }

        val registration = firestoreQuery.addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                Timber.d("Error: $exception")
                throw Exception(exception)
            }

            if (snapshots == null) {
                Timber.d("snapshots is null")
                return@addSnapshotListener
            }

            Timber.d("documents = ${snapshots.documents.size}")
            Timber.d("documentChanges = ${snapshots.documentChanges.size}")

            _expenses.update { currentExpenses ->

                Timber.d("Inside _expense.update :subscriptionId = ${subscriptionId}")
                val subscriptionExpenses =
                    currentExpenses[subscriptionId]?.toMutableMap() ?: mutableMapOf()

                for (dc in snapshots.documentChanges) {
                    val expense = dc.document.toObject(Expense::class.java)
                    Timber.d("Expense: $expense")
                    when (dc.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                            subscriptionExpenses[expense.id!!] = expense
                        }

                        DocumentChange.Type.REMOVED -> {
                            subscriptionExpenses.remove(expense.id!!)
                        }
                    }
                }

                currentExpenses + (subscriptionId to subscriptionExpenses)
            }
        }
        Timber.d("Start listening to $subscriptionId")
        listeners[subscriptionId] = registration
    }

    override fun stopListening(subscriptionId: String) {
        Timber.d("Stop listening to $subscriptionId")
        listeners[subscriptionId]?.remove()
        listeners.remove(subscriptionId)

        /* delete StateFlow from _expenses */
        _expenses.update { it - subscriptionId }
    }

    override suspend fun addExpense(expense: Expense): Expense {
        val document = expenseCollection.document()
        val newExpense = expense.copy(id = document.id)
        document.set(newExpense)
        return newExpense
    }

    override suspend fun updateExpense(expense: Expense): Expense {
        expenseCollection.document(expense.id!!).set(expense)
        return expense
    }

    override suspend fun removeExpense(id: String) {
        expenseCollection.document(id).delete()
    }
}