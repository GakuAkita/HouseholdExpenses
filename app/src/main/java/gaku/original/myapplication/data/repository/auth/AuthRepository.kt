package gaku.original.myapplication.data.repository.auth
import gaku.original.myapplication.domain.AppUser
import gaku.original.myapplication.domain.AuthState
import kotlinx.coroutines.flow.StateFlow

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