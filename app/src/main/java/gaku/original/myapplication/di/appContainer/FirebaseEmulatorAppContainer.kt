package gaku.original.myapplication.di.appContainer

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import gaku.original.myapplication.BuildConfig
import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.data.repository.auth.FirebaseAuthRepository
import gaku.original.myapplication.di.sessionContainer.FirebaseSessionContainer
import gaku.original.myapplication.di.sessionContainer.SessionContainer
import gaku.original.myapplication.service.ocr.MlkitOcrService
import gaku.original.myapplication.service.ocr.OcrService
import timber.log.Timber

/**
 * I totally forgot why I need to write this url.
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
        /* Android emulator: 10.0.2.2, real device: the PC's IP address. Set in emulator_host.txt */
        val host = BuildConfig.FIREBASE_EMULATOR_HOST
        Timber.d("Use emulators on $host")
        firebaseAuth.useEmulator(host, 9099)
        firestore.useEmulator(host, 5002)
        firebaseRealtimeDb.useEmulator(host, 9000)

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
        /* I have no idea what's happening. Even if this app is connected to Realtime Database Emulator, the connection is lost in several seconds....*/
        /* In the production environment, this doesn't happen. */
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
            categoryDao = appDatabase.categoryDao(),
            context = context
        )
    }
}