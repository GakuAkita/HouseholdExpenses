package gaku.original.myapplication.ui.view

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
import gaku.original.myapplication.ui.common.TopBarView
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
            Text("取得したデータ:${notificationData.value.toString()}")
            Text("タイトル:${notificationData.value?.title}")
            Text("テキスト:${notificationData.value?.text}")
        }
    }
}