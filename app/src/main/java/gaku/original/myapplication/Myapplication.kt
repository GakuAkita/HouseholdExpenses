package gaku.original.myapplication


import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import gaku.original.myapplication.di.appContainer.AppContainer
import gaku.original.myapplication.di.appContainer.FakeAppContainer
import gaku.original.myapplication.di.appContainer.FirebaseAuthTestAppContainer
import timber.log.Timber

class MyApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        // Firebase Local Emulatorの設定は、FirestoreReferenceとRealtimeDbReferenceのコンストラクタで行う
        // 注意: Firebase Authは本番環境を使用（Googleサインイン等のOAuthプロバイダー認証のため）
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        appContainer = FirebaseAuthTestAppContainer()

        Timber.d("MyApplication Created. hashCode =${hashCode()}")
    }
}