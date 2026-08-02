package gaku.original.myapplication


import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import dagger.hilt.android.HiltAndroidApp
import gaku.original.myapplication.di.appContainer.AppContainer
import gaku.original.myapplication.di.appContainer.FakeAppContainer

@HiltAndroidApp
class MyApplication : Application() {
    lateinit var appContainer: AppContainer
    private set

    override fun onCreate() {
        // Firebase Local Emulatorの設定は、FirestoreReferenceとRealtimeDbReferenceのコンストラクタで行う
        // 注意: Firebase Authは本番環境を使用（Googleサインイン等のOAuthプロバイダー認証のため）
        super.onCreate()

        appContainer = FakeAppContainer()
    }
}