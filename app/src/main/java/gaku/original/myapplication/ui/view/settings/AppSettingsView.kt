package gaku.original.myapplication.ui.view.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.Utility.AppTimeZone
import gaku.original.myapplication.ui.view.BottomBarView
import gaku.original.myapplication.ui.view.TopBarView
import gaku.original.myapplication.viewModel.AppSettingsViewModel

@Composable
fun AppSettingsView(
    viewModel: AppSettingsViewModel = hiltViewModel(),
    navController: NavController
) {
    val zoneId by AppTimeZone.zoneIdFlow.collectAsState()
    Scaffold(
        topBar = {
            TopBarView(
                "ユーザー情報",
                onBackNavClicked = { navController.popBackStack() },
                showBackButton = true,
            )
        },

        bottomBar = { BottomBarView(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            Text("現在の設定:${zoneId}")
            Button(
                onClick = {
                    viewModel.setUserTimeZone(
                        timeZone = "Asia/Tokyo",
                        callback = { statusInfo ->
                        }
                    )
                }
            ) { Text("設定読み込む") }
            Text("現在は変更不可能")
        }
    }
}