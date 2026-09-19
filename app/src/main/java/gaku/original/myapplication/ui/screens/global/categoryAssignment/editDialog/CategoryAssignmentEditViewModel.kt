package gaku.original.myapplication.ui.screens.global.categoryAssignment.editDialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.MatchCondition
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.categoryAssignment.CategoryAssignmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

data class CategoryAssignmentEditUiState(
    val isEdit: Boolean = false,
    val message: String? = null,
    val type: CategoryAssignmentType = CategoryAssignmentType.PRODUCT,
    val name: String? = "",
    val condition: MatchCondition = MatchCondition.EXACT,
    val categoryId: String? = null,

    val categories: List<Category> = emptyList()
)

enum class CategoryAssignmentType {
    PRODUCT,
    STORE
}

class CategoryAssignmentEditViewModel(
    private val assignment: CategoryAssignment?,
    private val categoryAssignmentRepository: CategoryAssignmentRepository,
    private val categoryRepository: CategoryRepository
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
                    session.categoryAssignmentRepository,
                    session.categoryRepository
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")
        _uiState.update {
            it.copy(
                isEdit = assignment != null,
                categories = categoryRepository.categories.value.values.toList()
            )
        }

        if (assignment != null) {
            when (assignment) {
                is CategoryAssignment.Product -> {
                    _uiState.update {
                        it.copy(
                            type = CategoryAssignmentType.PRODUCT,
                            name = assignment.name,
                            condition = assignment.condition,
                            categoryId = assignment.categoryId
                        )
                    }
                }

                is CategoryAssignment.Store -> {
                    _uiState.update {
                        it.copy(
                            type = CategoryAssignmentType.STORE,
                            name = assignment.name,
                            condition = assignment.condition,
                            categoryId = assignment.categoryId
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

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(
                name = name
            )
        }
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}