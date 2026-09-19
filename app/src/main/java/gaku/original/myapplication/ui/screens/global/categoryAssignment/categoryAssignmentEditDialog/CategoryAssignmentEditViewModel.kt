package gaku.original.myapplication.ui.screens.global.categoryAssignment.categoryAssignmentEditDialog

import androidx.lifecycle.ViewModel
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.repository.categoryAssignment.CategoryAssignmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

data class CategoryAssignmentEditUiState(
    val isEdit: Boolean = false,
    val message: String? = null
)

class CategoryAssignmentEditViewModel(
    private val assignment: CategoryAssignment?,
    private val categoryAssignmentRepository: CategoryAssignmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryAssignmentEditUiState())
    val uiState = _uiState.asStateFlow()

    init {
        Timber.d("Created. ${hashCode()}")
        if (assignment == null) {

        } else {

        }
    }

    fun onMessageShown() {
        _uiState.update {
            it.copy(
                message = null
            )
        }
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}