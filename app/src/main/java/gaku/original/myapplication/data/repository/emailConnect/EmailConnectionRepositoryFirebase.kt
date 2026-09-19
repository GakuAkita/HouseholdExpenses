package gaku.original.myapplication.data.repository.emailConnect

import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.BuildConfig
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.domain.AppUser
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailProvider
import kotlinx.coroutines.tasks.await

/* THis can be used only when Firebase is used for SignIn */
open class EmailConnectionRepositoryFirebase(
    private val appUser: AppUser,
    private val firebaseAuth: FirebaseAuth,
    private val realtimeDbReference: RealtimeDbUserReference
) : EmailConnectionRepository {
    private val reference = realtimeDbReference.gmailTokensReference.child(appUser.email!!)

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
        if (appUser.email == null) {
            throw Exception("Coding Error: Email is null")
        }
        when (provider) {
            EmailProvider.GMAIL -> {
                val snapshot = reference.get().await()
                return snapshot.exists()
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

class FakeEmailConnectionRepositoryFirebase(
    private val appUser: AppUser,
    private val firebaseAuth: FirebaseAuth,
    private val realtimeDbReference: RealtimeDbUserReference
) : EmailConnectionRepositoryFirebase(
    appUser = appUser,
    firebaseAuth = firebaseAuth,
    realtimeDbReference = realtimeDbReference
) {
    override suspend fun isConnected(provider: EmailProvider): Boolean {
        return false
    }
}