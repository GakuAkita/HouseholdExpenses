package gaku.original.myapplication.utility

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import gaku.original.myapplication.R

/**
 * 参考:
 * https://developer.android.com/develop/ui/views/notifications/build-notification?hl=ja
 */
fun createNotificationChannel(
    context: Context,
    channelId: String,
    channelName: String,
    descriptionText: String,
    importance: Int = NotificationManager.IMPORTANCE_DEFAULT
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            importance
        ).apply {
            description = descriptionText
        }

        /* システムにチャンネルを登録する */
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

/**
 * 基本的にはこれをラップして使う
 */
fun sendNotification(
    context: Context,
    channelId: String,
    @DrawableRes icon: Int = R.drawable.money_icon_foreground,
    title: String,
    text: String,
    notifyId: Int,
    pendingIntent: PendingIntent? = null/* 呼び出し側で生成して渡す */
) {
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(icon)
        .setContentTitle(title)
        .setContentText(text).apply {
            if (pendingIntent != null) {
                setContentIntent(pendingIntent)
                setAutoCancel(true)
            }
        }

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
        notify(notifyId, builder.build()) // IDが同じだと上書きされる
    }
}