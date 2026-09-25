package gaku.original.myapplication.domain

sealed interface AuthState {

    data object Loading : AuthState

    data object LoggedOut : AuthState

    data class LoggedIn(
        val user: AppUser
    ) : AuthState
}