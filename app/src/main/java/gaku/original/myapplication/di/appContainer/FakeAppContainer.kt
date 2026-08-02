package gaku.original.myapplication.di.appContainer

import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.data.repository.auth.FakeAuthRepository
import gaku.original.myapplication.di.sessionContainer.FakeSessionContainer
import gaku.original.myapplication.di.sessionContainer.SessionContainer

class FakeAppContainer: AppContainer() {

    override val authRepository: AuthRepository
        get() = FakeAuthRepository()

    override fun createSession() {
        _sessionContainer = FakeSessionContainer()
    }
}