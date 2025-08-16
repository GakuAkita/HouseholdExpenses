package gaku.original.myapplication.notificationListener

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import gaku.original.myapplication.MainActivity
import gaku.original.myapplication.data.Constants.AppPackageNames
import gaku.original.myapplication.data.Constants.IntentKey
import gaku.original.myapplication.data.Constants.IntentSourceKeys

class UniversalNotificationListenerService : NotificationListenerService() {
    private val TAG = "UniversalNLS"/* ログに使うだけ */

    private val targetApps = setOf(
        AppPackageNames.PAYPAY,
        AppPackageNames.THIS_APP
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            val pkgName = it.packageName
            if (pkgName !in targetApps) return

            val notification = it.notification
            val extras = notification.extras

            val title = extras.getString("android.title")
            val text = extras.getCharSequence("android.text").toString()

            when (pkgName) {
                AppPackageNames.PAYPAY,
                AppPackageNames.THIS_APP -> {
                    /**
                     * すでにアプリが開いていれば、画面を起動
                     * なければ通知でタップで起動できるようにする
                     */
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra(IntentKey, IntentSourceKeys.NOTIFICATION_LISTENER)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }

                    startActivity(intent)
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