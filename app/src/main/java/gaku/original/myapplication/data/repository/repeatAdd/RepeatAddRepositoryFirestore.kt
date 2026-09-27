package gaku.original.myapplication.data.repository.repeatAdd

import com.google.firebase.firestore.ListenerRegistration
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
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

            val repeatAdds = snapshots.documents.map { doc ->
                val repeatAdd = doc.toObject(RepeatAddDto::class.java)!!.toDomain()
                Timber.d("RepeatAdd: $repeatAdd")
                doc.id to repeatAdd
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
        val repeatAdds = snapshots.documents.map { document ->
            document.id to document.toObject(RepeatAddDto::class.java)!!.toDomain()
        }.toMap()
        return repeatAdds
    }

    override suspend fun addRepeatAdd(repeatAdd: RepeatAdd): RepeatAdd {
        repeatAddCollection.document(repeatAdd.id).set(repeatAdd.toDto()).await()
        Timber.d("addRepeatAdd Success: $repeatAdd")
        return repeatAdd
    }

    override suspend fun updateRepeatAdd(repeatAdd: RepeatAdd): RepeatAdd {
        repeatAddCollection.document(repeatAdd.id).set(repeatAdd.toDto()).await()
        return repeatAdd
    }

    override suspend fun deleteRepeatAdd(id: String) {
        repeatAddCollection.document(id).delete().await()
    }
}
