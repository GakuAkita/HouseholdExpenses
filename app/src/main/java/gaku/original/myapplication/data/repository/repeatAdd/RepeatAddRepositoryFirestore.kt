package gaku.original.myapplication.data.repository.repeatAdd

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.dataClass.toFirestore
import gaku.original.myapplication.data.dataClass.toRepeatFrequency
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import gaku.original.myapplication.data.repository.category.toCategory
import gaku.original.myapplication.data.repository.expense.toFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class RepeatAddRepositoryFirestore(
    private val reference: FirestoreUserReference
) : RepeatAddRepository {

    private val repeatAddCollection = reference.repeatAddCollection

    private val _repeatAdds = MutableStateFlow<Map<String, RepeatAdd>>(emptyMap())
    override val repeatAdds: StateFlow<Map<String, RepeatAdd>> = _repeatAdds

    private var registration: ListenerRegistration? = null

    override fun startListening() {
        Timber.d("Start Listening to RepeatAdds")
        registration = repeatAddCollection.addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                throw Exception(exception)
            }

            if (snapshots == null) return@addSnapshotListener

            val repeatAdds = snapshots.documents.mapNotNull { doc ->
                Timber.d("RepeatAdd: ${doc.toRepeatAdd()}")
                doc.toRepeatAdd().let { doc.id to it }
            }.toMap()

            _repeatAdds.value = repeatAdds
        }
    }

    override fun stopListening() {
        Timber.d("Stop Listening to RepeatAdds")
        registration?.remove()
        registration = null
    }

    override suspend fun getAllRepeatAdds(): Map<String, RepeatAdd> {
        val snapshots = repeatAddCollection.get().await()
        val repeatAdds = snapshots.documents.mapNotNull { document ->
            document.toRepeatAdd().let { document.id to it }
        }.toMap()
        return repeatAdds
    }

    override suspend fun addRepeatAdd(repeatAdd: RepeatAdd): RepeatAdd {
        val document = repeatAddCollection.document()
        val newRepeatAdd = repeatAdd.copy(id = document.id, timestamp = System.currentTimeMillis())
        document.set(newRepeatAdd.toFirestore()).await()
        Timber.d("addRepeatAdd Success: $newRepeatAdd")
        return newRepeatAdd
    }

    override suspend fun updateRepeatAdd(repeatAdd: RepeatAdd): RepeatAdd {
        if (repeatAdd.id == null) {
            throw Exception("Program Error: repeatAdd.id is null when updating")
        }
        repeatAddCollection.document(repeatAdd.id!!).set(repeatAdd.toFirestore()).await()
        return repeatAdd
    }

    override suspend fun deleteRepeatAdd(id: String) {
        repeatAddCollection.document(id).delete().await()
    }
}

fun RepeatAdd.toFirestore(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "timestamp" to timestamp,
        "expense" to expense.toFirestore(),
        "frequencyInfo" to frequencyInfo?.toFirestore()
    )
}

fun DocumentSnapshot.toRepeatAdd(): RepeatAdd {
    val expenseRaw = get("expense") as? Map<String, Any?>
    val frequencyInfoRaw = get("frequencyInfo") as? Map<String, Any?>

    return RepeatAdd(
        id = getString("id"),
        timestamp = getLong("timestamp"),
        expense = expenseRaw?.toExpenseForRepeatAdd() ?: error("expense is null"),
        frequencyInfo = frequencyInfoRaw?.toRepeatFrequency() ?: error("frequencyInfo is null")
    )
}

fun Map<String, Any?>.toExpenseForRepeatAdd(): Expense {
    val categoryRaw = get("category") as? Map<String, Any?>
    return Expense(
        category = categoryRaw?.toCategory() ?: error("category is null"),
        amount = get("amount") as Long? ?: error("amount is null"),
        storeName = get("storeName") as String?,
        itemName = get("itemName") as String?,
        note = get("note") as String?,
    )
}