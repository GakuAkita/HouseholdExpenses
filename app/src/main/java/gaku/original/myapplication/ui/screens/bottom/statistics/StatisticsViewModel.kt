package gaku.original.myapplication.ui.screens.bottom.statistics

import androidx.lifecycle.ViewModel
import timber.log.Timber

class StatisticsViewModel : ViewModel() {



    init {
        Timber.d("Created. ${hashCode()}")
    }

    override fun onCleared() {
        Timber.d("Cleared ${hashCode()}")
        super.onCleared()
    }

}