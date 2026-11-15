package gaku.original.myapplication.notificationListener

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import gaku.original.myapplication.MainActivity
import gaku.original.myapplication.R
import gaku.original.myapplication.data.Constants.AppPackageNames
import gaku.original.myapplication.data.Constants.IntentKey
import gaku.original.myapplication.data.Constants.IntentSourceKeys
import gaku.original.myapplication.data.Constants.NotificationChannels
import gaku.original.myapplication.data.dataClass.NotificationData
import gaku.original.myapplication.utility.sendNotification
import timber.log.Timber

class UniversalNotificationListenerService : NotificationListenerService() {
    private val TAG = "UniversalNLS"/* ログに使うだけ */

    override fun onListenerConnected() {
        /* Confirms in the file log that the listener is actually bound */
        Timber.tag(TAG).i("Notification listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            /* paypayには全く反応しないからこの方式は無理だ、、 */

            val pkgName = it.packageName
//            if (pkgName !in targetApps || pkgName == null) return

            val notification = it.notification
            val extras = notification.extras

            val title = extras.getString("android.title")
            val text = extras.getCharSequence("android.text")
            val timestamp: Long = it.postTime
//            if (title == null || text == null) {
//                return
//            }
            Timber.tag(TAG).d("pkg:$pkgName title:$title text:$text timestamp:$timestamp")

            if (pkgName == AppPackageNames.PAYPAY) {
                /* INFO so it is kept in the shareable file log while investigating PayPay detection */
                Timber.tag(TAG).i(
                    "PayPay notification: channel=${notification.channelId} " +
                            "title=${extras.getCharSequence(Notification.EXTRA_TITLE)} " +
                            "text=${extras.getCharSequence(Notification.EXTRA_TEXT)} " +
                            "bigText=${extras.getCharSequence(Notification.EXTRA_BIG_TEXT)} " +
                            "lines=${extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.joinToString(" | ")} " +
                            "keys=${extras.keySet()}"
                )
            }

            if (pkgName == AppPackageNames.THIS_APP ||
                pkgName == AppPackageNames.FELICA_NETWORKS
            ) {
                /**
                 * このアプリ自身の通知は無視する
                 */
                return
            }
            
            when (pkgName) {
                AppPackageNames.PAYPAY,
                AppPackageNames.NOTIFICATION_TESTER -> {
                    /**
                     * intentすべき通知のタイプか判断
                     * 送金の場合とかも通知が来るので。支払いのみに限る
                     */
                    val isCreateNotification = checkPayPayNotification(
                        title ?: "no title",
                        text ?: "no text"
                    )

                    /* 条件を満たしていない場合は弾く */
                    if (isCreateNotification) {
                        Timber.tag(TAG).d("This is paypay payment notification")
                    } else {
                        Timber.tag(TAG).d("This is NOT paypay payment notification")
                        return
                    }

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
    intent: Intent,
) {
    val pendingIntent = PendingIntent.getActivity(
        context,
        System.currentTimeMillis().toInt(),/* ここが固定だと、通知が近くても同じIntentが送られてしまう？ */
        intent,
        PendingIntent.FLAG_IMMUTABLE/* これを少なくともつけないとエラーになるらしい */
    )

    /**
     * これがPayPayの支払い後に通知として現れる
     */
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

fun checkPayPayNotification(title: String, text: CharSequence): Boolean {
    //Timber.d("title:${title} text:${text}")
//    if (title != "PayPay") return false
//
    if (!text.startsWith("取引が完了しました")) {
        return false
    }
//
//    if (!text.contains("金額")) {
//        return false
//    }

    /* ここまで来たら、支払いの通知である */
    return true
}

