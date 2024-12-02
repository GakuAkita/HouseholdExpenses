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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}