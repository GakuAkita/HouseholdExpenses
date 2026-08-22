package gaku.original.myapplication.di.appContainer

import android.content.Context
import gaku.original.myapplication.data.repository.auth.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.data.repository.auth.FirebaseAuthRepository
import gaku.original.myapplication.data.repository.emailConnect.EmailConnectionRepositoryFirebase
import gaku.original.myapplication.di.sessionContainer.FakeSessionContainer
import gaku.original.myapplication.ui.screens.start.signin.GoogleCredentialProvider

class FirebaseAuthTestAppContainer(
    private val context: Context
): FakeAppContainer() {
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    override val authRepository: AuthRepository = FirebaseAuthRepository(
        firebaseAuth = firebaseAuth,
        googleCredentialProvider = GoogleCredentialProvider(context)
    )

    override fun createSession() {
        _sessionContainer = FakeSessionContainer(
            emailConnectionRepository = EmailConnectionRepositoryFirebase(
                firebaseAuth
            )
        )
    }
}