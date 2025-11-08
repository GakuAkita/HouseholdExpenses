package gaku.original.myapplication


import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        // Firebase Local Emulatorの設定
        // 注意: Firebase Authは本番環境を使用（Googleサインイン等のOAuthプロバイダー認証のため）
        // FirestoreとRealtime Databaseのみエミュレータに接続
        // super.onCreate()の前に設定することで、Firestoreインスタンスが初期化される前にエミュレータ設定を行う
        if (BuildConfig.DEBUG) {
            try {
                // Firestoreエミュレータの設定
                // 注意: 
                // - Androidエミュレータからは "10.0.2.2" を使用（ホストマシンの127.0.0.1にアクセス）
                // - 実機からはホストマシンの実際のIPアドレスを使用（local.propertiesで設定）
                // - Firebase Emulator Suiteは通常 localhost:5002 でリッスン
                val emulatorHost = BuildConfig.FIREBASE_EMULATOR_HOST
                val emulatorPort = 5002
                
                Log.d("MyApplication", "🔧 エミュレータ設定開始: host=$emulatorHost, port=$emulatorPort")
                Log.d("MyApplication", "🔧 Firebase.firestoreにアクセス前")
                
                Firebase.firestore.useEmulator(emulatorHost, emulatorPort)
                
                Log.d("MyApplication", "✅ Firestore emulator configured: $emulatorHost:$emulatorPort")
                Log.d("MyApplication", "🔧 Firebase.firestoreにアクセス後")
                
                // Realtime Databaseのエミュレータ設定はRealtimeDbReference.ktで行っている
            } catch (e: Exception) {
                Log.e("MyApplication", "❌ Failed to configure Firestore エミュレーター: ${e.message}", e)
                Log.e("MyApplication", "❌ Error message: ${e.message}")
                Log.e("MyApplication", "❌ Error type: ${e.javaClass.simpleName}")
                e.printStackTrace()
                // エミュレータ設定に失敗してもアプリは続行
            }
        } else {
            Log.d("MyApplication", "ℹ️ DEBUGモードではないため、エミュレータ設定をスキップ")
        }
        super.onCreate()
    }
}