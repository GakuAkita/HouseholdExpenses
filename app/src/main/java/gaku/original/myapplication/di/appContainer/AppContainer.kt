package gaku.original.myapplication.di.appContainer

import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.di.sessionContainer.SessionContainer

abstract class AppContainer {
    abstract val authRepository: AuthRepository


    // Subclass can assign SessionContainer to _sessionContainer
    // because it is protected, not private
    protected var _sessionContainer: SessionContainer? = null

    val sessionContainer: SessionContainer?
        get() = _sessionContainer

    abstract fun createSession()

    fun clearSession(){
        _sessionContainer = null
    }
}