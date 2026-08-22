package gaku.original.myapplication.di.appContainer

import android.content.Context
import gaku.original.myapplication.data.repository.auth.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.data.repository.auth.FirebaseAuthRepository
import gaku.original.myapplication.data.repository.emailConnect.EmailConnectionRepositoryFirebase
import gaku.original.myapplication.di.sessionContainer.FakeSessionContainer
import gaku.original.myapplication.di.sessionContainer.SessionContainer
import gaku.original.myapplication.ui.screens.start.signin.GoogleCredentialProvider

class FirebaseAuthTestAppContainer: FakeAppContainer() {
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    override val authRepository: AuthRepository = FirebaseAuthRepository(
        firebaseAuth = firebaseAuth
    )

    override fun createSessionContainer(): SessionContainer {
        return FakeSessionContainer(
            emailConnectionRepository = EmailConnectionRepositoryFirebase(
                firebaseAuth
            )
        )
    }
}