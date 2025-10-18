package gaku.original.myapplication.data.dataClass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationData(
    val packageName: String,
    val title: String?,
    val text: CharSequence?,
    val timestamp: Long
) : Parcelable {
    companion object {
        const val EXTRA_KEY = "notification_data"
    }
}