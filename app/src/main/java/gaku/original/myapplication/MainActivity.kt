package gaku.original.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import gaku.original.myapplication.ui.view.navigateToOCRView
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.utility.getParcelableExtraCompat
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
                setArgsToSharedImageViewModel()
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

    /**
     * onCreateが起動されていないとonNewIntentは呼ばれない
     */
    override fun onNewIntent(intent: Intent?) {
        LogAkitaDebug("onNewIntent() called.")
        super.onNewIntent(intent)
        setIntent(intent)
        setArgsToSharedImageViewModel()
        LogAkitaDebug("In onNewIntent isFromShareReceiver=${sharedImageViewModel.isFromShareReceiver} isMovedToOCR=${sharedImageViewModel.isMovedToOCR}")

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            navController.navigate(Screen.StartScreen.Login.route) {
                launchSingleTop = true // 既に最上位にあれば再作成しない
                popUpTo(Screen.StartScreen.Login.route) {
                    inclusive = false // Login 自体は残す
                }
            }
        } else {
            Log.d("onNewIntent", "pile OCRRead on the stack")
            sharedImageViewModel.setIsMovedToOCR(true)
            navigateToOCRView(navController)
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

    /**
     * 必ずShareReceiverを
     */
    private fun setArgsToSharedImageViewModel() {
        val imageUri: Uri? =
            intent.getParcelableExtraCompat(ShareReceiverKeys.SHARED_IMAGE_URI)
        LogAkitaDebug("OCRDebug: URI to load:${imageUri}")
        val isFromSharedReceiver =
            intent?.getBooleanExtra(ShareReceiverKeys.IS_FROM_SHARE_RECEIVER, false) ?: false

        if (imageUri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }

        sharedImageViewModel.updateSharedImageUri(imageUri)
        sharedImageViewModel.setIsFromShareReceiver(isFromSharedReceiver)
        sharedImageViewModel.setIsMovedToOCR(false)
    }

    private fun decideDestination(): String {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val launchedByTap =
            intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
        LogAkitaDebug("decideDestination action:${Intent.ACTION_MAIN} category:${Intent.CATEGORY_LAUNCHER} launchedByTap:$launchedByTap")

        if (firebaseUser == null) {
            if (launchedByTap) {
                /**
                 * アイコンタップにより起動された
                 */
                return Screen.StartScreen.Start.route
            } else {
                /**
                 * 画像共有等で起動された
                 */
                return Screen.StartScreen.Login.route
            }
        } else {
            return Screen.MainScreen.Content.route
        }
    }
}
