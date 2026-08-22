package gaku.original.myapplication.di.appContainer

import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.data.repository.auth.FirebaseAuthRepository
import gaku.original.myapplication.data.repository.auth.AuthRepository

class FirebaseAuthTestAppContainer: FakeAppContainer() {
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    override val authRepository: AuthRepository = FirebaseAuthRepository(
        firebaseAuth = firebaseAuth
    )
}