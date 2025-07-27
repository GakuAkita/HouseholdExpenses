package gaku.original.myapplication.ui.view.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.Constants.Status.SignOutResult
import gaku.original.myapplication.ui.common.BottomBarView
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.start.AuthManagerViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsView(
    viewModel: AuthManagerViewModel = hiltViewModel(),
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            TopBarView("SettingsView")
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        bottomBar = { BottomBarView(navController) }
    ) { innerPadding ->
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SettingRow(
                onClick = {
                    navController.navigate(Screen.SettingScreen.UserInfo.route)
                }
            ) {
                Text("ユーザー情報", modifier = Modifier.padding(start = 10.dp))
            }

            SettingRow(
                onClick = {
                    navController.navigate(Screen.SettingScreen.AppSettings.route)
                }
            ) {
                //タイムゾーンはアラートでいいか。
                Text(text = "タイムゾーン設定", modifier = Modifier.padding(start = 10.dp))
            }

            SettingRow(
                onClick = {
                    navController.navigate(Screen.SettingScreen.RepeatAdd.route)
                }
            ) {
                Text("繰り返し自動追加", modifier = Modifier.padding(start = 10.dp))
            }

            SettingRow(
                onClick = {
                    navController.navigate(Screen.SettingScreen.MailboxExtraction.Main.route)
                }
            ) {
                //タイムゾーンはアラートでいいか。
                Text(text = "メールボックス自動抽出", modifier = Modifier.padding(start = 10.dp))
            }

            SettingRow(
                onClick = {
                    navController.navigate(Screen.GlobalScreen.CategoryAddEdit.route)
                }
            ) {
                //
                Text(text = "カテゴリー一覧", modifier = Modifier.padding(start = 10.dp))
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                onClick = {
                    /**
                     * サインアウトに失敗したときに、FirebaseAuthは空になっているけど、initしちゃったみたいなケースはあるか？？
                     * そこらへんの対処が必要になるか??
                     * */
                    //ログアウト機能を実装
                    val ret = viewModel.signOut()
                    if (ret == SignOutResult.SUCCESS) {
                        scope.launch {
                            snackBarHostState.showSnackbar("ログアウトしました")
                        }
                        navController.navigate(Screen.StartScreen.Start.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else if (ret == SignOutResult.SIGN_OUT_FAILED) {
                        scope.launch {
                            snackBarHostState.showSnackbar("ログアウトに失敗しました")
                        }
                    }
                }
            ) {
                Text("LogOut")
            }
        }
    }
}

@Composable
fun SettingRow(
    modifier: Modifier = Modifier, // カスタム Modifier を適用できる
    borderColor: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit = {},
    content: @Composable RowScope.() -> Unit // RowScope を適用
) {
    Row(
        modifier = Modifier
            .height(50.dp)
            .padding(horizontal = 10.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.onBackground, // Composable 関数内ならOK
                shape = RoundedCornerShape(8.dp)
            )
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
    Spacer(modifier = Modifier.padding(2.dp))
}