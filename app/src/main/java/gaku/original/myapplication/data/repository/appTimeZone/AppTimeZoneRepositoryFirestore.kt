package gaku.original.myapplication.data.repository.appTimeZone

import com.google.firebase.firestore.ListenerRegistration
import gaku.original.myapplication.common.CodingErrorException
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.ZoneId

class AppTimeZoneRepositoryFirestore(
    private val reference: FirestoreUserReference
) : AppTimeZoneRepository {
    private val document = reference.userPreferencesDocument

    private var registration: ListenerRegistration? = null

    private val _zoneId = MutableStateFlow(ZoneId.systemDefault())
    override val zoneId: StateFlow<ZoneId> = _zoneId.asStateFlow()

    override fun startListening() {
        if (registration != null) {
            throw CodingErrorException("Already listening to AppTimeZone")
        }
        registration = document.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Timber.e(exception)
                return@addSnapshotListener
            }
            if (snapshot == null) {
                Timber.e("No data")
                return@addSnapshotListener
            }
            val zoneId = snapshot.getString("timeZone")
            if (zoneId != null) {
                _zoneId.value = ZoneId.of(zoneId)
            } else {
                _zoneId.value = ZoneId.systemDefault()
            }
        }
    }

    override fun stopListening() {
        Timber.d("stopListening called")
        registration?.remove()
        registration = null
    }

    override suspend fun getZoneId(newZoneId: ZoneId): ZoneId {
        val snapshot = document.get().await()
        val zoneId = snapshot.getString("timeZone")
        if (zoneId != null) {
            return ZoneId.of(zoneId)
        }
        return ZoneId.systemDefault()
    }

    override suspend fun updateZoneId(newZoneId: ZoneId) {
        Timber.d(newZoneId.id)
        document.update("timeZone", newZoneId.id).await()
        return
    }

}