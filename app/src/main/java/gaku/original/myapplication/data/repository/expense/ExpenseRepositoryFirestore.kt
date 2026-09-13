package gaku.original.myapplication.data.repository.expense

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.toGeneratedType
import gaku.original.myapplication.data.repository.category.toCategory
import gaku.original.myapplication.data.repository.category.toFirestore
import gaku.original.myapplication.domain.AppUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
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

        query.datetimeFromOrEqual?.let {
            Timber.d("datetimeFromOrEqual=$it")
            firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo(
                "datetime", it.toString()
            )
        }

        query.datetimeTo?.let {
            Timber.d("datetimeTo=$it")
            firestoreQuery = firestoreQuery.whereLessThan(
                "datetime", it.toString()
            )
        }

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

                val subscriptionExpenses =
                    currentExpenses[subscriptionId]?.toMutableMap() ?: mutableMapOf()

                for (dc in snapshots.documentChanges) {
                    val expense = dc.document.toExpense()
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
        Timber.d("addExpense: ${expense.toFirestore()}")
        val document = expenseCollection.document()
        val newExpense = expense.copy(id = document.id)
        document.set(newExpense.toFirestore()).await()
        return newExpense
    }

    override suspend fun updateExpense(expense: Expense): Expense {
        expenseCollection.document(expense.id!!).set(expense.toFirestore()).await()
        return expense
    }

    override suspend fun removeExpense(id: String) {
        expenseCollection.document(id).delete().await()
    }
}

fun Expense.toFirestore(): Map<String, Any?> {
    /* Firestore functionsとルールを一致させる */
    return mapOf(
        "id" to id,
        "timestamp" to timestamp,
        "datetime" to datetime,
        "amount" to amount,
        "category" to category?.toFirestore(),
        "note" to note,
        "storeName" to storeName,
        "itemName" to itemName,
        "generatedType" to generatedType?.toSerialized()
    )
}

fun DocumentSnapshot.toExpense(): Expense {
    val categoryRaw = get("category") as? Map<String, Any?>
    return Expense(
        id = getString("id"),
        timestamp = getLong("timestamp"),
        datetime = getString("datetime"),
        amount = getLong("amount"),
        category = categoryRaw?.toCategory(),
        note = getString("note"),
        storeName = getString("storeName"),
        itemName = getString("itemName"),
        generatedType = getString("generatedType")?.toGeneratedType()
    )
}