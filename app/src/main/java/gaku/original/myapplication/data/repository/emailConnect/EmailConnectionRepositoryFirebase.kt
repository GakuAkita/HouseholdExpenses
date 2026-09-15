package gaku.original.myapplication.data.repository.emailConnect

import EmailProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import gaku.original.myapplication.BuildConfig
import kotlinx.coroutines.tasks.await

/* THis can be used only when Firebase is used for SignIn */
class EmailConnectionRepositoryFirebase(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseRealtimeDb: FirebaseDatabase
) : EmailConnectionRepository {
    private val reference = firebaseRealtimeDb.reference.child("users").

    private fun generateOAuthUrl(idToken: String): String {
        val baseUrl = "https://accounts.google.com/o/oauth2/v2/auth"
        val params = listOf(
            "client_id=${BuildConfig.WEB_CLIENT_ID}",
            "redirect_uri=${BuildConfig.REDIRECT_URI}",
            "response_type=code",
            "scope=email https://www.googleapis.com/auth/gmail.readonly",
            "access_type=offline",
            "prompt=consent",
            "state=$idToken"
        ).joinToString("&")
        // OAuthのURLを生成するロジックを実装
        // ここでは仮のURLを返す
        return "$baseUrl?$params"
    }

    override suspend fun isConnected(provider: EmailProvider): Boolean {
        when (provider) {
            EmailProvider.GMAIL -> {
                return true
            }

            EmailProvider.YAHOO -> {
                throw Exception("Yahoo automation is Not Implemented")
            }

            EmailProvider.OUTLOOK -> {
                throw Exception("Outlook automation is Not Implemented")
            }
        }
    }

    override suspend fun connect(provider: EmailProvider): EmailConnectionAction {
        return when (provider) {
            EmailProvider.GMAIL -> {
                val user = firebaseAuth.currentUser
                if (user == null) {
                    throw Exception("Bug: Not Signed-In with Firebase Authentication")
                }
                val tokenRet = user.getIdToken(true).await()
                val token = tokenRet.token
                if (token.isNullOrEmpty()) {
                    throw Exception("Firebase Token is null or empty")
                }
                val url = generateOAuthUrl(token)

                EmailConnectionAction.OpenUrl(
                    url = url
                )
            }

            else -> {
                throw Exception("Unknown provider: $provider")
            }
        }
    }

}