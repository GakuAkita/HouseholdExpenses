package gaku.original.myapplication.data.repository.auth

import kotlinx.coroutines.flow.StateFlow

sealed interface SignInRequest {
    data class Email(
        val email: String,
        val password: String
    ) : SignInRequest

    data class Google(
        val idToken: String
    ) : SignInRequest
}

sealed interface SignUpRequest {
    data class Email(
        val email: String,
        val password: String
    ) : SignUpRequest
}

interface AuthRepository {
    //val authState: StateFlow<A>

    suspend fun signIn(
        request: SignInRequest
    )

    suspend fun signUp(
        request: SignUpRequest
    )

    suspend fun singOut()
}