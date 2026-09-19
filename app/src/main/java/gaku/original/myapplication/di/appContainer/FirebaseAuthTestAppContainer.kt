package gaku.original.myapplication.di.appContainer

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.data.repository.auth.FirebaseAuthRepository
import gaku.original.myapplication.data.repository.emailConnect.FakeEmailConnectionRepositoryFirebase
import gaku.original.myapplication.di.sessionContainer.FakeSessionContainer
import gaku.original.myapplication.di.sessionContainer.SessionContainer
import timber.log.Timber

class FirebaseAuthTestAppContainer(
    context: Context
) : FakeAppContainer(
    context = context
) {
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val firebaseRealtimeDb = Firebase.database
    override val authRepository: AuthRepository = FirebaseAuthRepository(
        firebaseAuth = firebaseAuth
    )

    init {
        Timber.d("Use emulators")
        firebaseRealtimeDb.useEmulator("10.0.2.2", 9000)
    }

    override fun createSessionContainer(): SessionContainer {
        return FakeSessionContainer(
            emailConnectionRepository = FakeEmailConnectionRepositoryFirebase(
                authRepository.user!!,
                firebaseAuth = firebaseAuth,
                realtimeDbReference = RealtimeDbUserReference(
                    authRepository.user!!,
                    firebaseRealtimeDb
                )
            )
        )
    }
}