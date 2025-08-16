package gaku.original.myapplication.data

import gaku.original.myapplication.data.dataClass.NotificationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * NotificationListenerServiceからは
 * Activityを起動すると新しいタスクでしかできないので、このObjectを経由して発火させる
 */
object NotificationListenerDataStore {
    private val _data = MutableStateFlow<NotificationData?>(null)
    val data: StateFlow<NotificationData?> get() = _data

    fun set(data: NotificationData) {
        _data.value = data
    }

    fun clear() {
        _data.value = null
    }
}