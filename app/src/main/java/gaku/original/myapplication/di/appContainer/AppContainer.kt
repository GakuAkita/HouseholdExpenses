package gaku.original.myapplication.di.appContainer

import gaku.original.myapplication.data.repository.auth.AuthRepository

abstract class AppContainer {
    abstract val authRepository: AuthRepository
}