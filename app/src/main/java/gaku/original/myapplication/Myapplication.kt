package gaku.original.myapplication


import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // アプリケーション全体で必要な初期化処理をここに追加
    }
}