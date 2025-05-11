package gaku.original.myapplication.ui.view.settings

import android.widget.Toast
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.Constants.Status.SingOutResult
import gaku.original.myapplication.ui.view.BottomBarView
import gaku.original.myapplication.ui.view.TopBarView
import gaku.original.myapplication.viewModel.AuthManagerViewModel

@Composable
fun SettingsView(
    viewModel: AuthManagerViewModel = hiltViewModel(),
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopBarView("SettingsView作成中")
        },

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

            SettingRow {
                //タイムゾーンはアラートでいいか。
                Text(text = "タイムゾーン設定(未実装)", modifier = Modifier.padding(start = 10.dp))
            }

            SettingRow(
                onClick = {
                    navController.navigate(Screen.SettingScreen.RepeatAdd.route)
                }
            ) {
                Text("繰り返し自動追加", modifier = Modifier.padding(start = 10.dp))
            }

            SettingRow {
                //タイムゾーンはアラートでいいか。
                Text(text = "外部自動連携", modifier = Modifier.padding(start = 10.dp))
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
                    if (ret == SingOutResult.SUCCESS) {
                        Toast.makeText(context, "ログアウトしました", Toast.LENGTH_SHORT).show()
                        navController.navigate(Screen.StartScreen.Start.route)
                    } else if (ret == SingOutResult.SIGN_OUT_FAILED) {
                        Toast.makeText(context, "ログアウトに失敗しました", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            ) {
                Text("LogOut(仮)")
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