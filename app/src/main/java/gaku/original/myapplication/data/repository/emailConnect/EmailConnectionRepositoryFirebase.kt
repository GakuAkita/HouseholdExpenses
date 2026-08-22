package gaku.original.myapplication.data.repository.emailConnect

import EmailProvider
import com.google.firebase.auth.FirebaseAuth


class EmailConnectionRepositoryFirebase(
    private val firebaseAuth: FirebaseAuth
): EmailConnectionRepository {
    override suspend fun isConnected(provider: EmailProvider): Boolean {
        return true
    }

    override suspend fun connect(provider: EmailProvider) {
        TODO("Not yet implemented")
    }

}