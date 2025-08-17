package gaku.original.myapplication.ui.view

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.utility.navigateToSingle
import gaku.original.myapplication.viewModel.NotificationListenerProcessViewModel

@Composable
fun NotificationListenerProcessView(
    viewModel: NotificationListenerProcessViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val notificationData = viewModel.notificationData.collectAsState()

    Scaffold(
        topBar = {
            TopBarView(
                "通知検知解析",
                showBackButton = true,
                onBackNavClicked = {
                    navController.popBackStack()
                }
            )
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
        }
    }
}

fun navigateToNLProcess(navController: NavHostController) {
    val funcName = "navigateToNLProcess"
    Log.d(funcName, "${funcName} was called.")
    navigateToSingle(navController, Screen.GlobalScreen.NotificationListenerProcess.route)
}