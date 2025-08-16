package gaku.original.myapplication.ui.view.settings.menu

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavHostController
import gaku.original.myapplication.ui.common.TopBarView

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

private fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        "test_channel_id", // Builder で使うIDと同じ
        "Test Notifications", // 設定画面に表示される名前
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "Channel for test notifications"
    }
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

fun sendTestNotification(context: Context) {
    createNotificationChannel(context)

    val builder = NotificationCompat.Builder(context, "test_channel_id")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("テスト通知")
        .setContentText("これはローカル通知のテストです")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notify(1, builder.build()) // IDが同じだと上書きされる
    }
}