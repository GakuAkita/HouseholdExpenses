package gaku.original.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.dataClass.NotificationData
import gaku.original.myapplication.viewModel.shared.SharedNotificationListenerViewModel
import gaku.original.myapplication.viewModel.shared.TemporaryExpenseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationListenerProcessViewModel @Inject constructor(
    private val sharedNotificationListenerViewModel: SharedNotificationListenerViewModel,
    private val tmpExpenseViewModel: TemporaryExpenseViewModel
) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        clearNotificationData()
    }

    private val _notificationData =
        MutableStateFlow(sharedNotificationListenerViewModel.getNotificationData())
    val notificationData: StateFlow<NotificationData?> get() = _notificationData

    init {
        viewModelScope.launch {
            sharedNotificationListenerViewModel.notificationData.collect {
                _notificationData.value = it
            }
        }
    }

    fun clearNotificationData() {
        sharedNotificationListenerViewModel.clearNotificationData()
        //sharedNotificationListenerViewModel.setIsMovedToNLProcess(false)/* 念の為つけていてもいいかも */
    }
}