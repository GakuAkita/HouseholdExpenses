package gaku.original.myapplication.ui.screens.bottom.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.ui.common.SectionHeader

@Composable
fun SettingScreenRoot(
    rootNavController: NavHostController,
    viewModel: SettingViewModel = viewModel(factory = SettingViewModel.Factory)
) {
    fun navigateToSettingMenu(route: MainGraph.SettingMenu) {
        rootNavController.navigate(route)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackBarHostState = LocalSnackBarHostState.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackBarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    SettingScreen(
        uiState,
        onMenuClick = {
            navigateToSettingMenu(it)
        },
        onSignOutClick = {
            viewModel.onSignOutClick()
        }
    )
}

@Composable
fun SettingScreen(
    uiState: SettingUiState,
    onMenuClick: (MainGraph.SettingMenu) -> Unit,
    onSignOutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        SettingSection(title = "Account") {
            SettingRowWithNavigation(
                label = "User Information",
                icon = Icons.Outlined.AccountCircle,
                route = MainGraph.SettingMenu.UserInfo,
                onMenuClick = onMenuClick
            )

            SettingRowWithNavigation(
                label = "Timezone Setting",
                icon = Icons.Outlined.Public,
                route = MainGraph.SettingMenu.TimeZone,
                onMenuClick = onMenuClick
            )
        }

        SettingSection(title = "Expenses") {
            SettingRowWithNavigation(
                label = "Categories",
                icon = Icons.Outlined.Category,
                route = MainGraph.SettingMenu.Categories,
                onMenuClick = onMenuClick
            )

            SettingRowWithNavigation(
                label = "Repeat Add",
                icon = Icons.Outlined.Repeat,
                route = MainGraph.SettingMenu.IRepeatAdd.Screen,
                onMenuClick = onMenuClick
            )
        }

        SettingSection(title = "Automation & Import") {
            SettingRowWithNavigation(
                label = "Mailbox Extraction",
                icon = Icons.Outlined.Email,
                route = MainGraph.SettingMenu.MailboxExtraction,
                onMenuClick = onMenuClick
            )

            SettingRowWithNavigation(
                label = "Amazon Subscribe Item",
                icon = Icons.Outlined.ShoppingCart,
                route = MainGraph.SettingMenu.AmazonSubscribeItem,
                onMenuClick = onMenuClick
            )

            SettingRowWithNavigation(
                label = "PayPay Receipt Reader",
                icon = Icons.Outlined.DocumentScanner,
                route = MainGraph.SettingMenu.PayPayReceiptOCRSetting,
                onMenuClick = onMenuClick
            )

            SettingRowWithNavigation(
                label = "Import from Dr.Wallet",
                icon = Icons.Outlined.FileUpload,
                route = MainGraph.SettingMenu.DrWalletImport,
                onMenuClick = onMenuClick
            )

//            SettingRowWithNavigation(
//                label = "Notification Listener Setting",
//                route = MainGraph.SettingMenu.NotificationListenerSetting,
//                onMenuClick = onMenuClick
//            )
        }

        SettingSection(title = "About") {
            SettingRowWithNavigation(
                label = "App Version",
                icon = Icons.Outlined.Info,
                route = MainGraph.SettingMenu.AppVersion,
                onMenuClick = onMenuClick
            )
        }

        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            onClick = {
                onSignOutClick()
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }
    }

//            SettingRowWithNavigation(
//                label = "ユーザー情報",
//                navController = navController,
//                route = Screen.SettingScreen.UserInfo.route
//            )
//
//            SettingRowWithNavigation(
//                label = "タイムゾーン設定",
//                navController = navController,
//                route = Screen.SettingScreen.AppSettings.route
//            )
//
//            SettingRowWithNavigation(
//                label = "カテゴリー一覧",
//                navController = navController,
//                route = Screen.GlobalScreen.CategoryAddEdit.route
//            )
//
//            SettingRowWithNavigation(
//                label = "繰り返し自動追加",
//                navController = navController,
//                route = Screen.SettingScreen.RepeatAdd.route
//            )
//
//            SettingRowWithNavigation(
//                label = "メールボックス自動抽出",
//                navController = navController,
//                route = Screen.SettingScreen.MailboxExtraction.Main.route
//            )
//
//            SettingRowWithNavigation(
//                label = "Amazon定期便リスト",
//                navController = navController,
//                route = Screen.SettingScreen.AmazonSubscribeItems.route
//            )
//
////            SettingRowWithNavigation(
////                label = "プッシュ通知から費用抽出",
////                navController = navController,
////                route = Screen.SettingScreen.NotificationListenerSetting.route
////            )
//            SettingRowWithNavigation(
//                label = "PayPayレシートOCR設定",
//                navController = navController,
//                route = Screen.SettingScreen.PayPayReceiptOCRSetting.route
//            )
//
//            SettingRowWithNavigation(
//                label = "バージョン情報",
//                navController = navController,
//                route = Screen.SettingScreen.Version.route
//            )
//
//            Button(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 20.dp),
//                onClick = {
//                    /**
//                     * サインアウトに失敗したときに、FirebaseAuthは空になっているけど、initしちゃったみたいなケースはあるか？？
//                     * そこらへんの対処が必要になるか??
//                     * */
//                    //ログアウト機能を実装
//                    val ret = viewModel.signOut()
//                    if (ret == SignOutResult.SUCCESS) {
//                        scope.launch {
//                            snackBarHostState.showSnackbar("ログアウトしました")
//                        }
//                        navController.navigate(Screen.StartScreen.Start.route) {
//                            popUpTo(0) { inclusive = true }
//                        }
//                    } else if (ret == SignOutResult.SIGN_OUT_FAILED) {
//                        scope.launch {
//                            snackBarHostState.showSnackbar("ログアウトに失敗しました")
//                        }
//                    }
//                }
//            ) {
//                Text("LogOut")
//            }
}

@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    val uiState = SettingUiState()

    SettingScreen(
        uiState,
        onMenuClick = {},
        onSignOutClick = {}
    )
}

@Composable
private fun SettingSection(
    title: String,
    content: @Composable () -> Unit
) {
    SectionHeader(text = title, modifier = Modifier.padding(top = 8.dp))
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun SettingRowWithNavigation(
    label: String,
    icon: ImageVector,
    route: MainGraph.SettingMenu,
    onMenuClick: (MainGraph.SettingMenu) -> Unit,
) {
    SettingRow(
        onClick = {
            onMenuClick(route)
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingRow(
    modifier: Modifier = Modifier, // カスタム Modifier を適用できる
    onClick: () -> Unit = {},
    content: @Composable RowScope.() -> Unit // RowScope を適用
) {
    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}
