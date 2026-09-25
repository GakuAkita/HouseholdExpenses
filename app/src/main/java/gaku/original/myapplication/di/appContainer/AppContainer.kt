package gaku.original.myapplication.di.appContainer

import android.content.Context
import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.di.sessionContainer.SessionContainer
import gaku.original.myapplication.service.ocr.OcrService
import timber.log.Timber

abstract class AppContainer(
    protected val context: Context
) {
    abstract val authRepository: AuthRepository

    abstract val ocrService: OcrService

    init {
        Timber.d("AppContainer init:${hashCode()}")
    }

    // Subclass can assign SessionContainer to _sessionContainer
    // because it is protected, not private
    private var _sessionContainer: SessionContainer? = null

    val sessionContainer: SessionContainer?
        get() = _sessionContainer

    protected abstract fun createSessionContainer(): SessionContainer

    fun createSession() {
        _sessionContainer = createSessionContainer()
    }

    fun clearSession() {
        /* Close all ClosableRepositories */
        _sessionContainer?.categoryRepository?.close()
        _sessionContainer = null
    }
}