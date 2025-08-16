package gaku.original.myapplication.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.NotificationListenerDataStore
import gaku.original.myapplication.data.dataClass.NotificationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class NotificationListenerProcessViewModel @Inject constructor(
) : ViewModel() {

    private val _notificationData =
        MutableStateFlow(NotificationListenerDataStore.data.value)
    val notificationData: StateFlow<NotificationData?> get() = _notificationData

    fun clearNotificationData() {
        NotificationListenerDataStore.clear()
    }
}