package gaku.original.myapplication.ui.screens.global.categoryAssignment

import androidx.lifecycle.ViewModel
import timber.log.Timber

class CategoryAssignmentViewModel: ViewModel() {


    init {
        Timber.d("Created. ${hashCode()}")
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}