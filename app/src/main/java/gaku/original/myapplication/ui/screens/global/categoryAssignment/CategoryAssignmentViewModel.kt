package gaku.original.myapplication.ui.screens.global.categoryAssignment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.data.repository.categoryAssignment.CategoryAssignmentRepository
import timber.log.Timber

class CategoryAssignmentViewModel(
    private val categoryAssignmentRepository: CategoryAssignmentRepository
) : ViewModel() {

    companion object {
        val Factory = viewModelFactory {
            initializer {
                CategoryAssignmentViewModel(
                    
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}