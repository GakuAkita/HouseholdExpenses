package gaku.original.myapplication.ui.screens.global.categoryAssignment.categoryAssignmentEditDialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.repository.categoryAssignment.CategoryAssignmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

data class CategoryAssignmentEditUiState(
    val isEdit: Boolean = false,
    val message: String? = null,
    val type: CategoryAssignmentType = CategoryAssignmentType.PRODUCT,
    val name: String = "",
)

enum class CategoryAssignmentType {
    PRODUCT,
    STORE
}

class CategoryAssignmentEditViewModel(
    private val assignment: CategoryAssignment?,
    private val categoryAssignmentRepository: CategoryAssignmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryAssignmentEditUiState())
    val uiState = _uiState.asStateFlow()

    companion object {
        fun Factory(assignment: CategoryAssignment?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY]) as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                CategoryAssignmentEditViewModel(
                    assignment,
                    session.categoryAssignmentRepository
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")
        if (assignment == null) {
            _uiState.update {
                it.copy(
                    isEdit = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isEdit = true
                )
            }
            when (assignment) {
                is CategoryAssignment.Product -> {
                    _uiState.update {
                        it.copy(
                            type = CategoryAssignmentType.PRODUCT
                        )
                    }
                }

                is CategoryAssignment.Store -> {
                    _uiState.update {
                        it.copy(
                            type = CategoryAssignmentType.STORE
                        )
                    }
                }
            }
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