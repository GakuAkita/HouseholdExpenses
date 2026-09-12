package gaku.original.myapplication.data.repository.appTimeZone

import com.google.firebase.firestore.FirebaseFirestore
import gaku.original.myapplication.domain.AppUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.time.ZoneId

class AppTimeZoneRepositoryFirestore(
    private val appUser: AppUser,
    private val firestore: FirebaseFirestore
) : AppTimeZoneRepository {
    private val document =
        firestore.collection("users").document(appUser.id!!).collection("settings")
            .document("user_preferences")

    private val _zoneId = MutableStateFlow(ZoneId.systemDefault())
    override val zoneId: StateFlow<ZoneId> = _zoneId.asStateFlow()

    override fun startListening() {
        document.addSnapshotListener { snapshot, exception ->
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

    }

    override suspend fun getZoneId(newZoneId: ZoneId): ZoneId {
        val ret = document.get()
        return ZoneId.systemDefault()
    }

    override suspend fun updateZoneId(newZoneId: ZoneId) {
        Timber.d(newZoneId.id)
        return
    }

}