package gaku.original.myapplication


import android.app.Application
import gaku.original.myapplication.di.appContainer.AppContainer
import gaku.original.myapplication.di.appContainer.FirebaseAppContainer
import timber.log.Timber

class MyApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        // Firebase Local Emulatorの設定は、FirestoreReferenceとRealtimeDbReferenceのコンストラクタで行う
        super.onCreate()

        Timber.plant(Timber.DebugTree())

//        appContainer =
//            if (BuildConfig.DEBUG) FirebaseEmulatorAppContainer(this) else FirebaseAppContainer(this)
        appContainer = FirebaseAppContainer(this)

        Timber.d("MyApplication Created. hashCode =${hashCode()}")
    }
}