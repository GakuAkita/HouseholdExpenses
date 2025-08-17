package gaku.original.myapplication.notificationListener

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import gaku.original.myapplication.MainActivity
import gaku.original.myapplication.R
import gaku.original.myapplication.data.Constants.AppPackageNames
import gaku.original.myapplication.data.Constants.IntentKey
import gaku.original.myapplication.data.Constants.IntentSourceKeys
import gaku.original.myapplication.data.Constants.NotificationValidTitles
import gaku.original.myapplication.data.dataClass.NotificationData

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
            val postTimeMillis: Long = sbn.postTime
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

            when (pkgName) {
                AppPackageNames.PAYPAY,
                AppPackageNames.THIS_APP -> {
                    /**
                     * PayPayの通知を検知して、適切なものだったら
                     * このアプリの通知を出してタップでExpense生成まで行けるようにする
                     */
                    val intent = Intent(this, MainActivity::class.java).apply {
                        val data = NotificationData(pkgName, title, text, postTimeMillis)
                        putExtra(IntentKey, IntentSourceKeys.NOTIFICATION_LISTENER)
                        putExtra(NotificationData.EXTRA_KEY, data)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    val pendingIntent = PendingIntent.getActivity(
                        this,
                        System.currentTimeMillis().toInt(),
                        intent,
                        PendingIntent.FLAG_IMMUTABLE/* これを少なくともつけないとエラーになるらしい */
                    )

                    val notification = NotificationCompat.Builder(this, "家計簿")
                        .setSmallIcon(R.drawable.money_icon_foreground)
                        .setContentText("PayPayの支払いを検知しました")
                        .setContentText("タップして費用として追加する")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()

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

fun sendNotificationFromListener(
    context: Context
) {

}

