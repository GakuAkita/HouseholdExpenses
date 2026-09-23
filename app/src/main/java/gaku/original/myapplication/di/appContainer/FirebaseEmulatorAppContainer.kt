package gaku.original.myapplication.di.appContainer

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.data.repository.auth.FirebaseAuthRepository
import gaku.original.myapplication.di.sessionContainer.FirebaseSessionContainer
import gaku.original.myapplication.di.sessionContainer.SessionContainer
import gaku.original.myapplication.service.ocr.MlkitOcrService
import gaku.original.myapplication.service.ocr.OcrService
import timber.log.Timber

/**
 * I totally forgot why I need to write this url.
 * Without it, even if this app is connected to Realtime Database, the connection is lost in several seconds....
 */
val REALTIME_DATABASE_URL =
    "https://householdexpenses2-default-rtdb.asia-southeast1.firebasedatabase.app"

class FirebaseEmulatorAppContainer(
    context: Context
) : AppContainer(context = context) {

    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val firebaseRealtimeDb: FirebaseDatabase =
        FirebaseDatabase.getInstance(REALTIME_DATABASE_URL)

    init {
        Timber.d("Use emulators")
        firebaseAuth.useEmulator("10.0.2.2", 9099)
        firestore.useEmulator("10.0.2.2", 5002)
        firebaseRealtimeDb.useEmulator("10.0.2.2", 9000)

        firestore.collection("_connection_test").document("status")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Timber.d("Firestore Error: ${exception.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                Timber.d(
                    "Firestore: fromCache=${snapshot.metadata.isFromCache}"
                )
            }

        /* .info/connected is booked reference */
        firebaseRealtimeDb.getReference(".info/connected").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    val connected = p0.getValue(Boolean::class.java) ?: false
                    if (connected) {
                        Timber.d("Connected to Realtime Database")
                    } else {
                        Timber.d("Disconnected from Realtime Database")
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    Timber.d("Realtime Database Error: ${p0.message}")
                }
            }
        )
    }

    override val ocrService: OcrService = MlkitOcrService()

    override val authRepository: AuthRepository = FirebaseAuthRepository(firebaseAuth)

    override fun createSessionContainer(): SessionContainer {
        return FirebaseSessionContainer(
            appUser = authRepository.user!!,
            firebaseAuth = firebaseAuth,
            firestore = firestore,
            firebaseRealtimeDb = firebaseRealtimeDb,
            ocrService = ocrService,
            context = context
        )
    }
}