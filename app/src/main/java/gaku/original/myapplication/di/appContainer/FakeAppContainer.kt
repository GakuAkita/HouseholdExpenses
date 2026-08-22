package gaku.original.myapplication.di.appContainer

import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.data.repository.auth.FakeAuthRepository
import gaku.original.myapplication.di.sessionContainer.FakeSessionContainer

open class FakeAppContainer(
    override val authRepository: AuthRepository = FakeAuthRepository()
): AppContainer() {

    override fun createSession() {
        _sessionContainer = FakeSessionContainer()
    }
}