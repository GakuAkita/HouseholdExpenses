package gaku.original.myapplication.di.appContainer

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.data.repository.auth.FirebaseAuthRepository
import gaku.original.myapplication.data.repository.emailConnect.EmailConnectionRepositoryFirebase
import gaku.original.myapplication.di.sessionContainer.FakeSessionContainer
import gaku.original.myapplication.di.sessionContainer.SessionContainer

class FirebaseAuthTestAppContainer(
    context: Context
) : FakeAppContainer(
    context = context
) {
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