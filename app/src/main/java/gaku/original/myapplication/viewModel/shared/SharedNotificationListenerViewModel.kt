package gaku.original.myapplication.viewModel.shared

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.dataClass.NotificationData
import javax.inject.Inject

/**
 * @HiltViewModelをつけたら、どうやらアクティビティ内で単一になった気がする。
 * つけなくても動くは動くが、どうやら複数インスタンスが作られる形になっていそう。
 * まじでどういう仕組なのかわからん。
 */
@HiltViewModel
class SharedNotificationListenerViewModel @Inject constructor() : ViewModel() {
    /**
     * NotificationListenerの値を格納しておく
     */
    private var notificationData: NotificationData? = null

    var isMovedToNLProcess: Boolean = false

    fun setIsMovedToNLProcess(value: Boolean = true) {
        isMovedToNLProcess = value
    }

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