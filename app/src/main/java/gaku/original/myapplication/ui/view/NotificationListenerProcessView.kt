package gaku.original.myapplication.ui.view

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.utility.navigateToSingle
import gaku.original.myapplication.viewModel.NotificationListenerProcessViewModel
import kotlinx.coroutines.launch

@Composable
fun NotificationListenerProcessView(
    viewModel: NotificationListenerProcessViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val notificationData = viewModel.notificationData.collectAsState()

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(notificationData.value) {
        if (notificationData.value == null) {
            /* データがないなら戻る */
            navController.popBackStack()
            return@LaunchedEffect
        }

//        Toast.makeText(
//            navController.context,
//            "通知内容取り込み",
//            Toast.LENGTH_SHORT
//        ).show()
        viewModel.passExpenseFromNotificationData(
            callback = { statusInfo ->

            }
        )
        scope.launch {
            snackBarHostState.currentSnackbarData?.dismiss()
            snackBarHostState.showSnackbar("通知内容取り込みました")
        }
    }

    Scaffold(
        topBar = {
            TopBarView(
                "通知検知解析",
                showBackButton = true,
                onBackNavClicked = {
                    navController.popBackStack()
                }
            )
        },
        snackbarHost = {
            androidx.compose.material3.SnackbarHost(hostState = snackBarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text("タイトル:${notificationData.value?.title}")
            Text("テキスト:${notificationData.value?.text}")

            Text("ここから解析する")

            /**
             * 取引が完了しました。
             * 金額：3,186円
             * 取引番号：......
             * 店舗名：ハローズ
             * オートチャージ金額：10,000円
             * という感じになる。オートチャージしたから通知が来たのか？？
             */
        }
    }
}

fun navigateToNLProcess(navController: NavHostController) {
    val funcName = "navigateToNLProcess"
    Log.d(funcName, "${funcName} was called.")
    navigateToSingle(navController, Screen.GlobalScreen.NotificationListenerProcess.route)
}