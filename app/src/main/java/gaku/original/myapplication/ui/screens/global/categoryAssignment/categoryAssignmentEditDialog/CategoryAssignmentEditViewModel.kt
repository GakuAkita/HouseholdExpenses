package gaku.original.myapplication.ui.screens.global.categoryAssignment.categoryAssignmentEditDialog

import androidx.lifecycle.ViewModel
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.repository.categoryAssignment.CategoryAssignmentRepository
import timber.log.Timber

class CategoryAssignmentEditViewModel(
    private val assignment: CategoryAssignment,
    private val categoryAssignmentRepository: CategoryAssignmentRepository
) : ViewModel() {


    init {
        Timber.d("Created. ${hashCode()}")
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("Cleared. ${hashCode()}")
    }
}