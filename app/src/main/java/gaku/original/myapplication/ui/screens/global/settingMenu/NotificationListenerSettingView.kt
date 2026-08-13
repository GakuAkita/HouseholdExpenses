package gaku.original.myapplication.ui.screens.global.settingMenu

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import gaku.original.myapplication.R
import gaku.original.myapplication.data.Constants.NotificationChannels
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.utility.sendNotification

@Composable
fun NotificationListenerSettingView(
    navController: NavHostController
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopBarView("プッシュ通知から自動抽出機能",
                showBackButton = true,
                onBackNavClicked = {
                    navController.popBackStack()
                })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text("プッシュ通知が来たときにその中身から費用を作成する機能です")
            Text("PayPayのみ有効")
            Text("手順")
            Text("1.通知アクセス権限をこのアプリに与える")
            Text("2.PayPayのpush通知をONにし、支払い通知が届くようにする。PayPayの言語設定が英語だと支払い通知は届かないようです、、、そんなクソなことあるのか！")
            Text("3.このアプリのプッシュ通知をONにする")

            NotificationTestButton()

            Button(
                onClick = {
                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    context.startActivity(intent)
                }
            ) {
                Text("通知アクセス権限を開く")
            }
        }
    }
}

@Composable
fun NotificationTestButton() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            sendTestNotification(context)
        } else {
            Toast.makeText(context, "通知権限が拒否されました", Toast.LENGTH_SHORT).show()
        }
    }

    Button(
        onClick = {
            sendTestNotification(context)
        }
    ) {
        Text("テスト通知を送信")
    }
}

fun sendTestNotification(context: Context) {
    sendNotification(
        context = context,
        channelId = NotificationChannels.Test.id,
        icon = R.drawable.money_icon_foreground,
//        title = "テスト通知",
//        text = "テスト通知です",
        title = "PayPay",
        text = "取引が完了しました。\n金額：1,234円\n店舗名：ハローズ\n取引番号:",
        notifyId = 1
    )
}