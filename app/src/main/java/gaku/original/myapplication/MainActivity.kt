package gaku.original.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import gaku.original.myapplication.ui.theme.HouseholdExpensesTheme
import gaku.original.myapplication.utility.LogAkitaDebug
import timber.log.Timber

val LocalSnackBarHostState = compositionLocalOf<SnackbarHostState> {
    error("SnackbarHostState state should be initialized at runtime")
}
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
//    private val sharedImageViewModel: SharedImageViewModel by viewModels()
//    private val sharedNotificationListenerViewModel: SharedNotificationListenerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val splashScreen = installSplashScreen()
//        splashScreen.setKeepOnScreenCondition { true }
        //https://www.youtube.com/watch?v=_Jslt5sMuKc

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
                            appContainer = (application as MyApplication).appContainer
                        )
                    }

                }
            }
        }
    }

    /**
     * onCreateが起動されていないとonNewIntentは呼ばれない
     */
//    override fun onNewIntent(intent: Intent?) {
//        LogAkitaDebug("onNewIntent() called.")
//        super.onNewIntent(intent)
//        if (intent == null) return
//        setIntent(intent)
//
//        /**
//         * ソースキーで共有画像なのか通知から起動されたのか区別する
//         */
//        val intentSourceKey = intent.getStringExtra(IntentKey)
//
//        val firebaseUser = FirebaseAuth.getInstance().currentUser
//
//        Log.d("onNewIntent", "source key:${intentSourceKey}")
//        when (intentSourceKey) {
//            IntentSourceKeys.SHARE_IMAGE_FOR_OCR -> {
//                /* PayPay以外に出てきたらどうしよう、、 */
//                setArgsToSharedImageViewModel()
//                /**
//                 * 画面共有から起動された
//                 */
//                if (firebaseUser == null) {
//                    navigateToSingle(navController, Screen.StartScreen.Login.route)
//                } else {
//                    Log.d("onNewIntent", "pile OCRRead on the stack")
//                    sharedImageViewModel.setIsMovedToOCR(true)
//                    navigateToOCREntryView(navController)
//                }
//            }
//
//            IntentSourceKeys.NOTIFICATION_LISTENER -> {
//                /* sharedViewModelに格納して、画面遷移 */
//                /* データの抽出とかは遷移先で行う */
//                setArgsToSharedNotificationListenerViewModel()
//
//                /**
//                 * 通知検知から来たIntent
//                 * 画面作成用のViewを用意してそこでExpenseを作成するか
//                 */
//                if (firebaseUser == null) {
//                    navigateToSingle(navController, Screen.StartScreen.Login.route)
//                } else {
//                    sharedNotificationListenerViewModel.setIsMovedToNLProcess(true)
//                    /**
//                     * NotificationListenerProcessViewが存在しているときにnavigateしたら
//                     * 前の情報は消して新しく更新することにする。
//                     * ここを普通のnavControllerにすれば、複数画面立ち上げられる。
//                     *
//                     */
//                    navigateToNLProcess(navController)
//                }
//            }
//
//            else -> {
//
//            }
//        }
//    }

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

    /**
     * 必ずShareReceiverを
     */
    private fun setArgsToSharedImageViewModel() {
//        val passedData = intent.getParcelableExtraCompat<SharedImageData>(SharedImageData.EXTRA_KEY)
//
//        val imageUri = passedData?.imageUri
//        LogAkitaDebug("OCRDebug: URI to load:${imageUri}")
//
//        if (imageUri != null) {
//            try {
//                contentResolver.takePersistableUriPermission(
//                    imageUri,
//                    Intent.FLAG_GRANT_READ_URI_PERMISSION
//                )
//            } catch (e: SecurityException) {
//                e.printStackTrace()
//            }
//        }
//        sharedImageViewModel.updateSharedImageData(passedData)
//        sharedImageViewModel.setIsMovedToOCR(false)
    }

    /**
     * 通知検知から来た場合
     */
    private fun setArgsToSharedNotificationListenerViewModel() {
//        val notificationData =
//            intent.getParcelableExtraCompat<NotificationData>(NotificationData.EXTRA_KEY)
//        LogAkitaDebug("Received notification data:${notificationData}")
//        sharedNotificationListenerViewModel.setIsMovedToNLProcess(false)
//        sharedNotificationListenerViewModel.setNotificationData(notificationData)
    }


//    private fun decideDestination(): String {
//        val firebaseUser = FirebaseAuth.getInstance().currentUser
//        val launchedByTap =
//            intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
//        LogAkitaDebug("decideDestination action:${Intent.ACTION_MAIN} category:${Intent.CATEGORY_LAUNCHER} launchedByTap:$launchedByTap")
//
//        if (firebaseUser == null) {
//            if (launchedByTap) {
//                /**
//                 * アイコンタップにより起動された
//                 */
//                return Screen.StartScreen.Start.route
//            } else {
//                /**
//                 * 画像共有等で起動された
//                 */
//                return Screen.StartScreen.Login.route
//            }
//        } else {
//            return Screen.MainScreen.Content.route
//        }
//    }
}
