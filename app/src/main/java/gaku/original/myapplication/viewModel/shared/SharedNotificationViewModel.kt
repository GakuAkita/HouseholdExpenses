package gaku.original.myapplication.viewModel.shared

import androidx.lifecycle.ViewModel
import gaku.original.myapplication.data.dataClass.NotificationData
import javax.inject.Inject

class SharedNotificationViewModel @Inject constructor(

) : ViewModel() {
    /**
     * NotificationListenerの値を格納しておく
     */
    private var notificationData: NotificationData? = null

    fun setNotificationData(data: NotificationData) {
        notificationData = data
    }

    fun getNotificationData(): NotificationData? {
        return notificationData
    }
}