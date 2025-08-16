package gaku.original.myapplication.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.dataClass.NotificationData
import gaku.original.myapplication.viewModel.shared.SharedNotificationListenerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class NotificationListenerProcessViewModel @Inject constructor(
    private val sharedNotificationViewModel: SharedNotificationListenerViewModel
) : ViewModel() {

    private val _notificationData =
        MutableStateFlow(sharedNotificationViewModel.getNotificationData())
    val notificationData: StateFlow<NotificationData?> get() = _notificationData

    fun clearNotificationData() {
        sharedNotificationViewModel.clearNotificationData()
    }
}