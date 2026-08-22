package gaku.original.myapplication.data.repository.auth

import gaku.original.myapplication.domain.AppUser
import gaku.original.myapplication.domain.AuthState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class FakeAuthRepository : AuthRepository {
    private val appUser = AppUser(
        id = "sample",
        email = "g@gmail.com"
    )

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState>
        get() = _authState

    init {
        Timber.d("Created: ${hashCode()}")
        _authState.value = AuthState.LoggedIn(appUser)
    }

    override suspend fun signIn(request: SignInRequest): AppUser {
        Timber.d("signIn() called.")
        delay(3000)
        _authState.value = AuthState.LoggedIn(appUser)
        return appUser
    }

    override suspend fun signUp(request: SignUpRequest): AppUser {
        _authState.value = AuthState.LoggedIn(appUser)
        return appUser
    }

    override suspend fun signOut() {
        _authState.value = AuthState.LoggedOut
        return
    }
}