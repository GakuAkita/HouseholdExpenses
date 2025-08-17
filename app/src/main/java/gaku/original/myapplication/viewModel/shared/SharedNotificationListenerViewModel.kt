package gaku.original.myapplication.viewModel.shared

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.dataClass.NotificationData
import javax.inject.Inject

@HiltViewModel
class SharedNotificationListenerViewModel @Inject constructor() : ViewModel() {
    /**
     * NotificationListenerの値を格納しておく
     */
    private var notificationData: NotificationData? = null

    fun setNotificationData(data: NotificationData?) {
        notificationData = data
    }

    fun getNotificationData(): NotificationData? {
        return notificationData
    }

    fun clearNotificationData() {
        notificationData = null
    }
}