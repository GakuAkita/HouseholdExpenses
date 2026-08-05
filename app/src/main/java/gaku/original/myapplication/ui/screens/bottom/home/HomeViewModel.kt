package gaku.original.myapplication.ui.screens.bottom.home

import androidx.lifecycle.ViewModel
import timber.log.Timber

class HomeViewModel: ViewModel() {

    init {
        Timber.d("Created. ${hashCode()}")

    }

    override fun onCleared() {
        Timber.d("onCleared called. ${hashCode()}")
        super.onCleared()
    }
}