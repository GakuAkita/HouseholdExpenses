package gaku.original.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import gaku.original.myapplication.data.Constants.ShareReceiverKeys
import gaku.original.myapplication.ui.theme.HouseholdExpensesTheme
import gaku.original.myapplication.ui.view.Navigation.Navigation
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.viewModel.SharedImageViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private val sharedImageViewModel: SharedImageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val splashScreen = installSplashScreen()
//        splashScreen.setKeepOnScreenCondition { true }
        //https://www.youtube.com/watch?v=_Jslt5sMuKc

        setContent {
            navController = rememberNavController()
            HouseholdExpensesTheme(
                darkTheme = true/*システム設定によらずずっとダーク*/
            ) {
                // 一番最初にデフォルで存在するScaffold
                // HedgehogだとデフォルトでSurfaceがあってやりやすかったのでそっちをパクる。
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        val startDestination = decideDestination()
                        Navigation(navController, startDestination = startDestination)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        LogAkitaDebug("onNewIntent() called.")
        super.onNewIntent(intent)
        setIntent(intent)
        setArgsToSharedImageViewModel()
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

    /**
     * 必ずShareReceiverを
     */
    private fun setArgsToSharedImageViewModel() {
        val imageUri = intent?.getStringExtra(ShareReceiverKeys.SHARED_IMAGE_URI)
        val isFromSharedReceiver =
            intent?.getBooleanExtra(ShareReceiverKeys.IS_FROM_SHARE_RECEIVER, false) ?: false

        sharedImageViewModel.updateSharedImageUri(imageUri)
        sharedImageViewModel.setIsFromShareReceiver(isFromSharedReceiver)
        sharedImageViewModel.setIsMovedToOCR(false)
    }

    private fun decideDestination(): String {
        setArgsToSharedImageViewModel()
        val isFromShareReceiver = sharedImageViewModel.isFromShareReceiver

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (isFromShareReceiver) {
            if (firebaseUser == null) {
                return Screen.StartScreen.Login.route
            } else {
                return Screen.MainScreen.Content.route
            }
        } else {
            if (firebaseUser == null) {
                return Screen.StartScreen.Start.route
            } else {
                return Screen.MainScreen.Content.route
            }
        }
    }
}
