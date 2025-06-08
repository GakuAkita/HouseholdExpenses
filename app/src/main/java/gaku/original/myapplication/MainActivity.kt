package gaku.original.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import gaku.original.myapplication.ui.theme.HouseholdExpensesTheme
import gaku.original.myapplication.utility.LogAkitaDebug

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val splashScreen = installSplashScreen()
//        splashScreen.setKeepOnScreenCondition { true }
        //https://www.youtube.com/watch?v=_Jslt5sMuKc

        setContent {
            HouseholdExpensesTheme(
                darkTheme = true/*システム設定によらずずっとダーク*/
            ) {
                // 一番最初にデフォルで存在するScaffold
                // HedgehogだとデフォルトでSurfaceがあってやりやすかったのでそっちをパクる。
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        Navigation()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //起動時になにかしたいとき
        LogAkitaDebug("onStart() called. Do nothing.")
    }

    override fun onResume() {
        super.onResume()
        LogAkitaDebug("onResume() called. Do nothing.")
    }

    override fun onPause() {
        super.onPause()
        //一時停止時になにかしたいとき。ユーザーは見えている
        LogAkitaDebug("onPause() called. Do nothing.")
    }

    override fun onStop() {
        super.onStop()
        //アプリ終了時になにかしたいとき。ユーザーは何も見えない。
        LogAkitaDebug("onStart() called. Do nothing.")
    }

    override fun onRestart() {
        super.onRestart()
        //再起動時になにかしたいとき
        LogAkitaDebug("onRestart() called. Do nothing.")
    }

    override fun onDestroy() {
        super.onDestroy()
        //破棄時になにかしたいとき
        LogAkitaDebug("onDestroy() called. Do nothing.")
    }
}
