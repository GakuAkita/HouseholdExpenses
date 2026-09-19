package gaku.original.myapplication.ui.screens.global.categoryAssignment.categoryAssignmentEditDialog

import androidx.lifecycle.ViewModel
import timber.log.Timber

class CategoryAssignmentEditViewModel : ViewModel() {

    init {

    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("Cleared. ${hashCode()}")
    }
}