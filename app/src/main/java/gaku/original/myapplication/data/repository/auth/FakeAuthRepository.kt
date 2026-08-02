package gaku.original.myapplication.data.repository.auth

import gaku.original.myapplication.domain.AppUser
import gaku.original.myapplication.domain.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthRepository: AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState>
        get ()=_authState

    override suspend fun signIn(request: SignInRequest): AppUser {
        val user = AppUser(id = "1",email = "a@gmail.com")
        _authState.value = AuthState.LoggedIn(user)
        return user
    }

    override suspend fun signUp(request: SignUpRequest): AppUser {
        val user = AppUser(id="2",email = "a@gmail.com")
        _authState.value = AuthState.LoggedIn(user)
        return user
    }

    override suspend fun signOut() {
        _authState.value = AuthState.LoggedOut
        return
    }
}