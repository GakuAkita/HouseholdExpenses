package gaku.original.myapplication.notificationListener

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import gaku.original.myapplication.data.Constants.AppPackageNames

class UniversalNotificationListenerService : NotificationListenerService() {
    private val TAG = "UniversalNLS"/* ログに使うだけ */

    private val targetApps = setOf(
        AppPackageNames.PAYPAY
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val pkgName = sbn?.packageName
        if (pkgName !in targetApps) return

        val notification = sbn?.notification
        val extras = notification?.extras
        val title = extras?.getString("android.title")
        val text = extras?.getCharSequence("android.text")?.toString()
        Log.d(TAG, "onNotificationPosted: $title, $text")

        when (pkgName) {
            AppPackageNames.PAYPAY -> {

            }
        }
    }

}