package gaku.original.myapplication.data.repository.expense

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class ExpenseRepositoryFirestore(
    reference: FirestoreUserReference
) : ExpenseRepository {
    private val expenseCollection = reference.expenseCollection

    /* key is subscription id. */
    private val listeners = mutableMapOf<String, ListenerRegistration>()

    private val _expenses = MutableStateFlow<Map<String, Map<String, Expense>>>(emptyMap())
    override val expenses: StateFlow<Map<String, Map<String, Expense>>>
        get() = _expenses

    override fun startListening(subscriptionId: String, query: ExpenseQuery) {
        // すでにリスナーがある場合は何もしない（あるいは再起動するかは要件次第だが、一旦重複回避）
        if (listeners.containsKey(subscriptionId)) return
        val registration = buildQuery(query).addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                /* Throwing here crashes the app on the main thread. PERMISSION_DENIED arrives
                   right after sign-out, before the ViewModel stops listening. */
                Timber.e(exception)
                return@addSnapshotListener
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
                    val expense = dc.document.toObject(ExpenseDto::class.java).toDomain()
                    when (dc.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                            subscriptionExpenses[expense.id] = expense
                        }

                        DocumentChange.Type.REMOVED -> {
                            subscriptionExpenses.remove(expense.id)
                        }
                    }
                }

                currentExpenses + (subscriptionId to subscriptionExpenses)
            }
        }
        Timber.d("Start listening to $subscriptionId")
        listeners[subscriptionId] = registration
    }

    override suspend fun getExpenses(query: ExpenseQuery): List<Expense> =
        buildQuery(query).get().await()
            .documents
            .mapNotNull { it.toObject(ExpenseDto::class.java)?.toDomain() }

    private fun buildQuery(query: ExpenseQuery): Query {
        var firestoreQuery: Query = expenseCollection

        query.datetimeFromOrEqual?.let {
            Timber.d("datetimeFromOrEqual=$it")
            firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo("datetime", it.toDatetimeString())
        }

        query.datetimeTo?.let {
            Timber.d("datetimeTo=$it")
            firestoreQuery = firestoreQuery.whereLessThan("datetime", it.toDatetimeString())
        }

        return firestoreQuery
    }

    override fun stopListening(subscriptionId: String) {
        Timber.d("Stop listening to $subscriptionId")
        listeners[subscriptionId]?.remove()
        listeners.remove(subscriptionId)

        /* delete StateFlow from _expenses */
        _expenses.update { it - subscriptionId }
    }

    override suspend fun addExpense(expense: Expense): Expense {
        Timber.d("addExpense: ${expense.toDto()}")
        expenseCollection.document(expense.id).set(expense.toDto()).await()
        return expense
    }

    override suspend fun addExpenses(expenses: List<Expense>, onProgress: (Int) -> Unit) {
        var saved = 0
        /* A Firestore batch can hold up to 500 writes. */
        expenses.chunked(MAX_BATCH_WRITES).forEach { chunk ->
            val batch = expenseCollection.firestore.batch()
            chunk.forEach { batch.set(expenseCollection.document(it.id), it.toDto()) }
            batch.commit().await()
            saved += chunk.size
            onProgress(saved)
        }
    }

    override suspend fun updateExpense(expense: Expense): Expense {
        expenseCollection.document(expense.id).set(expense.toDto()).await()
        return expense
    }

    override suspend fun removeExpense(id: String) {
        expenseCollection.document(id).delete().await()
    }

    private companion object {
        const val MAX_BATCH_WRITES = 500
    }
}
