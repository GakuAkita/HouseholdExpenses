package gaku.original.myapplication


import android.app.Application
import gaku.original.myapplication.di.appContainer.AppContainer
import gaku.original.myapplication.di.appContainer.FakeAppContainer
import gaku.original.myapplication.di.appContainer.FirebaseAppContainer
import gaku.original.myapplication.di.appContainer.FirebaseEmulatorAppContainer
import gaku.original.myapplication.logging.FileLoggingTree
import gaku.original.myapplication.logging.RotatingLogFile
import timber.log.Timber
import java.io.File

class MyApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    /* Keeps INFO and above on the device so the log can be shared from Settings. */
    lateinit var fileLoggingTree: FileLoggingTree
        private set

    override fun onCreate() {
        // Firebase Local Emulatorの設定は、FirestoreReferenceとRealtimeDbReferenceのコンストラクタで行う
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        fileLoggingTree = FileLoggingTree(RotatingLogFile(File(filesDir, "logs")))
        Timber.plant(fileLoggingTree)
        Timber.i("App started. version=${BuildConfig.VERSION_NAME} buildType=${BuildConfig.BUILD_TYPE}")

        /* In debug builds, APP_CONTAINER is chosen by the run configuration next to the build button. See app/build.gradle.kts */
        appContainer = when {
            !BuildConfig.DEBUG -> FirebaseAppContainer(this)
            BuildConfig.APP_CONTAINER == "fake" -> FakeAppContainer(this)
            else -> FirebaseEmulatorAppContainer(this)
        }
        Timber.d("AppContainer: ${appContainer::class.simpleName}")

        Timber.d("MyApplication Created. hashCode =${hashCode()}")
    }
}
