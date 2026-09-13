package gaku.original.myapplication.data.repository.repeatAdd

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.dataClass.toFirestore
import gaku.original.myapplication.data.repository.expense.toFirestore
import gaku.original.myapplication.domain.AppUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class RepeatAddRepositoryFirestore(
    private val appUser: AppUser,
    private val firestore: FirebaseFirestore
) : RepeatAddRepository {

    private val repeatAddCollection =
        firestore.collection("users").document(appUser.id!!).collection("repeat_add")

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
                doc.toObject(RepeatAdd::class.java)
                    ?.let { doc.id to it }
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
            document.toObject(RepeatAdd::class.java)
                ?.let { document.id to it }
        }.toMap()
        return repeatAdds
    }

    override suspend fun addRepeatAdd(repeatAdd: RepeatAdd): RepeatAdd {
        val document = repeatAddCollection.document()
        val newRepeatAdd = repeatAdd.copy(id = document.id)
        document.set(newRepeatAdd.toFirestore()).await()
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
    
}