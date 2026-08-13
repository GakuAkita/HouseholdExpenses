package gaku.original.myapplication.ui.screens.global.settingMenu.version

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import gaku.original.myapplication.BuildConfig
import gaku.original.myapplication.ui.common.BottomBarView
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun VersionScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopBarView(
                "Application Version",
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
                Text("Version Name: ")
                Text(BuildConfig.VERSION_NAME)
            }
            Row(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Version Code: ")
                Text("${BuildConfig.VERSION_CODE}")
            }
        }
    }
}

@Preview
@Composable
fun VersionScreenPreview()
{
    val navController = rememberNavController()
    VersionScreen(
        navController
    )
}

