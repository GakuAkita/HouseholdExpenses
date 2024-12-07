package gaku.original.myapplication

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import gaku.original.myapplication.ui.theme.HouseholdExpensesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HouseholdExpensesTheme(
                darkTheme = true/*システム設定によらずずっとダーク*/
            ) {
                val sharedViewModel: SharedViewModel by viewModels()

                // デバイスIDを取得してSharedViewModelにセット
                val deviceId = getDeviceId(applicationContext)
                sharedViewModel.setDeviceId(deviceId)

                // 一番最初にデフォルで存在するScaffold
                // HedgehogだとデフォルトでSurfaceがあってやりやすかったのでそっちをパクる。
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        Navigation(sharedViewModel)
                    }
                }
            }
        }
    }
}


// デバイスの一意なIDを取得する関数
private fun getDeviceId(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}