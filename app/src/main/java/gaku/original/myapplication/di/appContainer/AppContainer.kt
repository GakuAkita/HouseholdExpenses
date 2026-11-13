package gaku.original.myapplication.di.appContainer

import android.content.Context
import androidx.room.Room
import gaku.original.myapplication.data.datasource.room.AppDatabase
import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.di.sessionContainer.SessionContainer
import gaku.original.myapplication.service.ocr.OcrService
import timber.log.Timber

abstract class AppContainer(
    protected val context: Context
) {
    abstract val authRepository: AuthRepository

    abstract val ocrService: OcrService

    /* Local backup of data stored in Firebase. Shared by all users on this device. */
    protected val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            /* It only holds backups, so it is fine to drop it when the schema changes. */
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    init {
        Timber.d("AppContainer init:${hashCode()}")
    }

    // Subclass can assign SessionContainer to _sessionContainer
    // because it is protected, not private
    private var _sessionContainer: SessionContainer? = null

    /*
     * Created on first access while a user is signed in.
     * After the process is killed in the background, Android restores the screens (e.g. Home) and
     * their ViewModels read this before RootNavigation's LaunchedEffect runs, so it must not
     * depend on createSession() having been called.
     */
    val sessionContainer: SessionContainer?
        @Synchronized get() {
            if (_sessionContainer == null && authRepository.user != null) {
                _sessionContainer = createSessionContainer()
            }
            return _sessionContainer
        }

    protected abstract fun createSessionContainer(): SessionContainer

    /* Does nothing if a session already exists, so the listeners of the old one don't leak. */
    @Synchronized
    fun createSession() {
        if (_sessionContainer == null) {
            _sessionContainer = createSessionContainer()
        }
    }

    @Synchronized
    fun clearSession() {
        /* Close all ClosableRepositories */
        _sessionContainer?.categoryRepository?.close()
        _sessionContainer = null
    }
}