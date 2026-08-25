package gaku.original.myapplication.ui.screens.receiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.shareReceiver.SharedData
import timber.log.Timber


class ShareReceiverViewModel(
    private val sharedData: SharedData
) : ViewModel() {

    companion object {
        fun Factory(sharedData: SharedData): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ShareReceiverViewModel(
                    sharedData
                )
            }
        }
    }

    init {
        Timber.d("init() called.${hashCode()}")
    }

    override fun onCleared() {
        Timber.d("onCleared() called.${hashCode()}")
        super.onCleared()
    }
}