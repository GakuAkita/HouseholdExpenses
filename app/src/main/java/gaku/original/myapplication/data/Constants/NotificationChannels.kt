package gaku.original.myapplication.data.Constants

import android.app.NotificationManager
import android.content.Context
import gaku.original.myapplication.utility.createNotificationChannel

sealed class NotificationChannels(
    val id: String,
    val name: String,
    val description: String,
    val importance: Int
) {
    object PayPayDetection : NotificationChannels(
        id = "detect_paypay_notification",
        name = "PayPay支払い通知を検知し費用を自動生成する通知",
        description = "これをONするのに加えて、PayPayのプッシュ通知をON、通知アクセス権限をこのアプリに与えてください",
        importance = android.app.NotificationManager.IMPORTANCE_HIGH
    )

    object Test : NotificationChannels(
        id = "test_notification",
        name = "テスト通知",
        description = "テスト用の通知チャンネルです",
        importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
    )

    companion object {
        val all: List<NotificationChannels> by lazy {
            NotificationChannels::class.sealedSubclasses.mapNotNull { it.objectInstance }
        }
    }
}

fun createAllNotificationChannelsWithRemove(context: Context) {
    createAllNotificationChannels(context)
    removeObsoleteNotificationChannels(context)
}

fun createAllNotificationChannels(context: Context) {
    NotificationChannels.all.forEach { channel ->
        createNotificationChannel(
            context = context,
            channelId = channel.id,
            channelName = channel.name,
            descriptionText = channel.description
        )
    }
}

fun removeObsoleteNotificationChannels(context: Context) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /* 存在するチャンネルID一覧 */
    val existChannels = notificationManager.notificationChannels.map { it.id }

    /* 定義されているチャンネル */
    val validChannelIds = NotificationChannels.all.map { it.id }/* 引数で渡したほうがいいか？？ */

    existChannels.filter { it !in validChannelIds }.forEach { obsoleteId ->
        notificationManager.deleteNotificationChannel(obsoleteId)
    }
}