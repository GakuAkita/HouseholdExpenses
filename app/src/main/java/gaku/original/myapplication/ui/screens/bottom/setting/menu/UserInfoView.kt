package gaku.original.myapplication.ui.screens.bottom.setting.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import gaku.original.myapplication.ui.common.BottomBarView
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun UserInfoView(
    //viewModel: AuthManagerViewModel = hiltViewModel(),
    navController: NavController
) {
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    top = 30.dp,
                )
                .padding(horizontal = 10.dp)
        ) {
            Row {
                Text("Email:")
                //Text("${viewModel.email}")
            }
        }
    }
}