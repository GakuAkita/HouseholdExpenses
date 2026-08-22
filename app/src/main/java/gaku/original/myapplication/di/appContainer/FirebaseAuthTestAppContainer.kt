package gaku.original.myapplication.di.appContainer

import gaku.original.myapplication.data.repository.auth.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.data.repository.auth.FirebaseAuthRepository
import gaku.original.myapplication.data.repository.emailConnect.EmailConnectionRepositoryFirebase
import gaku.original.myapplication.di.sessionContainer.FakeSessionContainer

class FirebaseAuthTestAppContainer: FakeAppContainer() {
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    override val authRepository: AuthRepository = FirebaseAuthRepository(
        firebaseAuth = firebaseAuth
    )

    override fun createSession() {
        _sessionContainer = FakeSessionContainer(
            emailConnectionRepository = EmailConnectionRepositoryFirebase(
                firebaseAuth
            )
        )
    }
}