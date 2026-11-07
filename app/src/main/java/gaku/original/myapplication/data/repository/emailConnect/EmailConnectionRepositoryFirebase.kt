package gaku.original.myapplication.data.repository.emailConnect

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ServerValue
import com.google.firebase.functions.FirebaseFunctions
import gaku.original.myapplication.BuildConfig
import gaku.original.myapplication.common.CodingErrorException
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.domain.AppUser
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import kotlin.time.Duration.Companion.milliseconds

/* THis can be used only when Firebase is used for SignIn */
open class EmailConnectionRepositoryFirebase(
    private val appUser: AppUser,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFunctions: FirebaseFunctions,
    private val realtimeDbReference: RealtimeDbUserReference
) : EmailConnectionRepository {
    private val convertedEmail = appUser.email!!.replace(".", "__dot__").replace("@", "__at__")

    init {
        Timber.d("Converted email = $convertedEmail")
    }

    private val reference = realtimeDbReference.gmailTokensReference.child(convertedEmail)

    /**
     * The OAuth state is "<uid>.<nonce>". It isn't a credential: the callback accepts it only
     * when the nonce matches the one this app wrote to gmail_oauth_request, and only once.
     * codeChallenge is the PKCE hash of the code verifier, which never goes in the URL.
     */
    private fun generateOAuthUrl(state: String, codeChallenge: String): String =
        Uri.parse("https://accounts.google.com/o/oauth2/v2/auth").buildUpon()
            .appendQueryParameter("client_id", BuildConfig.WEB_CLIENT_ID)
            .appendQueryParameter("redirect_uri", BuildConfig.REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", "email https://www.googleapis.com/auth/gmail.readonly")
            .appendQueryParameter("access_type", "offline")
            .appendQueryParameter("prompt", "consent")
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()
            .toString()

    override suspend fun isConnected(provider: EmailProvider): Boolean {
        if (appUser.email == null) {
            throw CodingErrorException("Email is null")
        }

        Timber.d("Checking if connected to $provider")
        when (provider) {
            EmailProvider.GMAIL -> {
                /* I have no idea what's happening. Immediately after installing, it works fine, but several seconds later, disconnected? */
                Timber.d("Checking if connected to Gmail. ref=$reference")
                val snapshot = withTimeout(5000.milliseconds) { reference.get().await() }
                Timber.d("Checking if connected to Gmail done.")
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
                if (firebaseAuth.currentUser == null) {
                    throw Exception("Bug: Not Signed-In with Firebase Authentication")
                }
                val uid = appUser.id ?: throw CodingErrorException("User id is null")

                /* One-time values for this connection. See functions/src/jobs/gmailOAuthCallback.ts. */
                val nonce = randomBase64Url()
                val codeVerifier = randomBase64Url()
                withTimeout(5000.milliseconds) {
                    realtimeDbReference.gmailOAuthRequestReference.setValue(
                        mapOf(
                            "nonce" to nonce,
                            "codeVerifier" to codeVerifier,
                            "createdAt" to ServerValue.TIMESTAMP
                        )
                    ).await()
                }
                val url = generateOAuthUrl(
                    state = "$uid.$nonce",
                    codeChallenge = codeChallengeOf(codeVerifier)
                )

                EmailConnectionAction.OpenUrl(
                    url = url
                )
            }

            else -> {
                throw Exception("Unknown provider: $provider")
            }
        }
    }

    override suspend fun disconnect(provider: EmailProvider) {
        when (provider) {
            EmailProvider.GMAIL -> {
                /* Done by Cloud Functions: only they can decrypt the token and remove it. */
                firebaseFunctions.getHttpsCallable("disconnectGmail").call().await()
            }

            else -> {
                throw Exception("Unknown provider: $provider")
            }
        }
    }
}

/** 32 random bytes as base64url without padding (43 characters). */
private fun randomBase64Url(): String {
    val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}

/** PKCE S256: base64url(SHA-256(code verifier)) without padding. */
private fun codeChallengeOf(codeVerifier: String): String {
    val hash = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray(Charsets.US_ASCII))
    return Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
}

class FakeEmailConnectionRepositoryFirebase(
    private val appUser: AppUser,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFunctions: FirebaseFunctions,
    private val realtimeDbReference: RealtimeDbUserReference
) : EmailConnectionRepositoryFirebase(
    appUser = appUser,
    firebaseAuth = firebaseAuth,
    firebaseFunctions = firebaseFunctions,
    realtimeDbReference = realtimeDbReference
) {
    override suspend fun isConnected(provider: EmailProvider): Boolean {
        return false
    }
}