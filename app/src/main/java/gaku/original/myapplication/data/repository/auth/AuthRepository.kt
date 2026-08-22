package gaku.original.myapplication.data.repository.auth
import android.app.Activity
import gaku.original.myapplication.domain.AppUser
import gaku.original.myapplication.domain.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {

    val user: AppUser?

    val authState: StateFlow<AuthState>

    suspend fun signIn(
        request: SignInRequest
    ): AppUser

    suspend fun signUp(
        request: SignUpRequest
    ): AppUser

    suspend fun signOut()
}
sealed interface SignInRequest {
    data class Email(
        val email: String,
        val password: String
    ) : SignInRequest

    data object Google: SignInRequest
}

sealed interface SignUpRequest {
    data class Email(
        val email: String,
        val password: String
    ) : SignUpRequest
}

interface GoogleSignIn{
    /* What if signInWithGoogle doesn't require Activity? */
    /* At least, unless we use firebase, we need activity. */
    suspend fun signInWithGoogle(
        activity: Activity
    ): AppUser
}