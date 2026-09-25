package gaku.original.myapplication

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import gaku.original.myapplication.data.Constants.createAllNotificationChannelsWithRemove
import gaku.original.myapplication.ui.navigation.RootNavigation
import gaku.original.myapplication.ui.screens.RootViewModel
import gaku.original.myapplication.ui.theme.HouseholdExpensesTheme
import timber.log.Timber

val LocalSnackBarHostState = compositionLocalOf<SnackbarHostState> {
    error("SnackbarHostState state should be initialized at runtime")
}

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    private val rootViewModel: RootViewModel by viewModels {
        RootViewModel.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootViewModel.onNewIntent(intent)

        /* 通知チャンネルをリセットする。いらないのを消して必要なのを生成 */
        createAllNotificationChannelsWithRemove(this)

        setContent {
            HouseholdExpensesTheme(
                darkTheme = true/*システム設定によらずずっとダーク*/
            ) {

                val snackbarHostState = remember { SnackbarHostState() }
                navController = rememberNavController()

                // https://developer.android.com/develop/ui/compose/compositionlocal
                CompositionLocalProvider(
                    LocalSnackBarHostState provides snackbarHostState
                ) {
                    /**
                     * onCreateされていないとonNewIntentは走らない
                     * したがって、ここでもsetArgsをしておかないとだめ。
                     */
                    //setArgsToSharedImageViewModel()
                    //setArgsToSharedNotificationListenerViewModel()
                    // 一番最初にデフォルで存在するScaffold
                    // HedgehogだとデフォルトでSurfaceがあってやりやすかったのでそっちをパクる。
                    Surface(modifier = Modifier.fillMaxSize()) {
                        RootNavigation(
                            navController,
                            appContainer = (application as MyApplication).appContainer,
                            viewModel = rootViewModel
                        )
                    }

                }
            }
        }
    }

    // https://zenn.dev/1stscratch/articles/1ea5e38cb9252c
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.d("onNewIntent() called.")
        setIntent(intent)

        rootViewModel.onNewIntent(intent)
    }

    /**
     * onCreateが起動されていないとonNewIntentは呼ばれない?
     */
    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        Timber.d("onNewIntent() called. 22")
        super.onNewIntent(intent, caller)
    }

    override fun onStart() {
        super.onStart()
        //起動時になにかしたいとき
        Timber.d("onStart() called. Do nothing.")
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume() called. Do nothing.")
    }

    override fun onPause() {
        super.onPause()
        //一時停止時になにかしたいとき。ユーザーは見えている
        Timber.d("onPause() called. Do nothing.")
    }

    override fun onStop() {
        super.onStop()
        //アプリ終了時になにかしたいとき。ユーザーは何も見えない。
        Timber.d("onStart() called. Do nothing.")
    }

    override fun onRestart() {
        super.onRestart()
        //再起動時になにかしたいとき
        Timber.d("onRestart() called. Do nothing.")
    }

    override fun onDestroy() {
        super.onDestroy()
        //破棄時になにかしたいとき
        Timber.d("onDestroy() called. Do nothing.")
    }
}