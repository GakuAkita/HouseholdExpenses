package gaku.original.myapplication.ui.screens

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController

@Composable
fun NotificationListenerProcessView(
    navController: NavHostController
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember {
        SnackbarHostState()
    }

//    LaunchedEffect(notificationData.value) {
//        if (notificationData.value == null) {
//            /* データがないなら戻る */
//            navController.popBackStack()
//            return@LaunchedEffect
//        }
//
////        Toast.makeText(
////            navController.context,
////            "通知内容取り込み",
////            Toast.LENGTH_SHORT
////        ).show()
//        viewModel.passExpenseFromNotificationData(
//            callback = { statusInfo ->
//                if (statusInfo.status == FuncStatus.SUCCESS) {
//                    /* すでにTmpExpenseにexpenseは移動しているのであとはnavigateだけ */
//                    Toast.makeText(
//                        context,
//                        "通知内容取り込みに成功しました",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    /* PayPayでどうなるか見るために画面遷移はしない */
////                    navController.navigate(Screen.GlobalScreen.ExpenseAddEdit.route) {
////                        popUpTo(Screen.GlobalScreen.NotificationListenerProcess.route) {
////                            inclusive = true // NLを削除
////                        }
////                    }
//                } else {
//                    scope.launch {
//                        snackBarHostState.currentSnackbarData?.dismiss()
//                        snackBarHostState.showSnackbar(
//                            "通知内容取り込みに失敗しました。${statusInfo.errorMessage}",
//                            actionLabel = "OK"
//                        )
//                    }
//                }
//            }
//        )
//    }
//
//    Scaffold(
//        topBar = {
//            TopBarView(
//                "通知検知解析",
//                showBackButton = true,
//                onBackNavClicked = {
//                    navController.popBackStack()
//                }
//            )
//        },
//        snackbarHost = {
//            SnackbarHost(hostState = snackBarHostState)
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//        ) {
//            Text("パッケージ名：${notificationData.value?.packageName}")
//            Text("タイトル:${notificationData.value?.title}")
//            Text("テキスト:${notificationData.value?.text}")
//
//            Text("ここから解析する")
//
//            /**
//             * 取引が完了しました。
//             * 金額：3,186円
//             * 取引番号：......
//             * 店舗名：ハローズ
//             * オートチャージ金額：10,000円
//             * という感じになる。オートチャージしたから通知が来たのか？？
//             */
//        }
//    }
}

fun navigateToNLProcess(navController: NavHostController) {
    val funcName = "navigateToNLProcess"
    Log.d(funcName, "${funcName} was called.")
    //navigateToSingle(navController, Screen.GlobalScreen.NotificationListenerProcess.route)
}