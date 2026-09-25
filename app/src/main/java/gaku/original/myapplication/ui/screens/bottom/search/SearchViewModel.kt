package gaku.original.myapplication.ui.screens.bottom.search

import androidx.lifecycle.ViewModel
import timber.log.Timber

class SearchViewModel : ViewModel() {
    init {
        Timber.d("Created. ${hashCode()}")
    }

    override fun onCleared() {
        Timber.d("onCleared called. ${hashCode()}")
        super.onCleared()
    }
}