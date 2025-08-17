package gaku.original.myapplication.notificationListener

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import gaku.original.myapplication.MainActivity
import gaku.original.myapplication.R
import gaku.original.myapplication.data.Constants.AppPackageNames
import gaku.original.myapplication.data.Constants.IntentKey
import gaku.original.myapplication.data.Constants.IntentSourceKeys
import gaku.original.myapplication.data.Constants.NotificationChannels
import gaku.original.myapplication.data.Constants.NotificationValidTitles
import gaku.original.myapplication.data.dataClass.NotificationData
import gaku.original.myapplication.utility.sendNotification

class UniversalNotificationListenerService : NotificationListenerService() {
    private val TAG = "UniversalNLS"/* ログに使うだけ */

    private val targetApps = NotificationValidTitles.appValidTitlesMap.keys

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            val pkgName = it.packageName
            if (pkgName !in targetApps || pkgName == null) return

            val notification = it.notification
            val extras = notification.extras

            val title = extras.getString("android.title")
            val text = extras.getCharSequence("android.text")
            val timestamp: Long = System.currentTimeMillis()
            if (title == null || text == null) {
                return
            }

            /**
             * titleでもフィルターをする
             */
            val validTitles = NotificationValidTitles.appValidTitlesMap[pkgName] ?: emptySet()
            if (title !in validTitles) {
                return
            }

            Log.d("UniversalNLS", "title:$title text:$text")

            when (pkgName) {
                AppPackageNames.PAYPAY,
                AppPackageNames.THIS_APP,
                AppPackageNames.NOTIFICATION_TESTER -> {
                    /**
                     * PayPayの通知を検知して、適切なものだったら
                     * このアプリの通知を出してタップでExpense生成まで行けるようにする
                     */
                    val intent = Intent(this, MainActivity::class.java).apply {
                        val data = NotificationData(pkgName, title, text, timestamp)
                        putExtra(IntentKey, IntentSourceKeys.NOTIFICATION_LISTENER)
                        putExtra(NotificationData.EXTRA_KEY, data)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }

                    sendNotificationFromNLSForPayPay(
                        this,
                        intent
                    )
                }

//                AppPackageNames.THIS_APP -> {
//                    Log.d(TAG, "これはテストで送られたものです。たぶん")
//                }

                else -> {
                    /* 関係ない通知 */
                }
            }
        }
    }
}

fun sendNotificationFromNLSForPayPay(
    context: Context,
    intent: Intent
) {
    val pendingIntent = PendingIntent.getActivity(
        context,
        System.currentTimeMillis().toInt(),/* ここが固定だと、通知が近くても同じIntentが送られてしまう？ */
        intent,
        PendingIntent.FLAG_IMMUTABLE/* これを少なくともつけないとエラーになるらしい */
    )

    sendNotification(
        context,
        channelId = NotificationChannels.PayPayDetection.id,
        icon = R.drawable.money_icon_foreground,
        title = "PayPayの支払いを検知しました",
        text = "タップして費用として追加する",
        notifyId = System.currentTimeMillis().toInt(),
        pendingIntent = pendingIntent
    )
}

