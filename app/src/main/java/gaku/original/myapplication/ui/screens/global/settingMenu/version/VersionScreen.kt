package gaku.original.myapplication.ui.screens.global.settingMenu.version

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import gaku.original.myapplication.BuildConfig
import gaku.original.myapplication.ui.common.InfoRow
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column {
                    InfoRow(
                        label = "Version Name",
                        value = BuildConfig.VERSION_NAME,
                        icon = Icons.Outlined.Info
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                    InfoRow(
                        label = "Version Code",
                        value = "${BuildConfig.VERSION_CODE}",
                        icon = Icons.Outlined.Tag
                    )
                }
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

