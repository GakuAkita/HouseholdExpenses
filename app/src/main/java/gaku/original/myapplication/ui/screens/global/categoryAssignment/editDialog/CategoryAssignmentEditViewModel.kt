package gaku.original.myapplication.ui.screens.global.categoryAssignment.editDialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.common.AppError
import gaku.original.myapplication.common.AppResult
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.MatchCondition
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.categoryAssignment.CategoryAssignmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class CategoryAssignmentEditUiState(
    val isEdit: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null,
    val isSaved: Boolean = false,

    val type: CategoryAssignmentType? = null,
    val name: String? = "",
    val condition: MatchCondition = MatchCondition.EXACT,
    val categoryId: String? = null,
    val isRegex: Boolean = false,

    val categories: List<Category> = emptyList()
)

enum class CategoryAssignmentType {
    PRODUCT,
    STORE
}

sealed interface CategoryAssignmentError : AppError {
    data object TypeIsEmpty : CategoryAssignmentError {
        override val message: String
            get() = "Assignment Type is empty."
    }

    data object NameIsEmpty : CategoryAssignmentError {
        override val message: String
            get() = "Name is empty."
    }

    data object MatchConditionEmpty : CategoryAssignmentError {
        override val message: String
            get() = "Match condition is empty."
    }

    data object CategoryIsEmpty : CategoryAssignmentError {
        override val message: String
            get() = "Category is empty."
    }
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
                            categoryId = assignment.categoryId,
                            isRegex = assignment.regex
                        )
                    }
                }

                is CategoryAssignment.Store -> {
                    _uiState.update {
                        it.copy(
                            type = CategoryAssignmentType.STORE,
                            name = assignment.name,
                            condition = assignment.condition,
                            categoryId = assignment.categoryId,
                            isRegex = assignment.regex
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

    fun onTypeSelected(type: CategoryAssignmentType) {
        _uiState.update {
            it.copy(
                type = type
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

    fun onRegexClick() {
        _uiState.update {
            it.copy(
                isRegex = !it.isRegex
            )
        }
    }

    fun onMatchConditionSelected(condition: MatchCondition) {
        _uiState.update {
            it.copy(
                condition = condition
            )
        }
    }

    fun onCategorySelected(categoryId: String?) {
        _uiState.update {
            it.copy(
                categoryId = categoryId
            )
        }
    }

    fun createCategoryAssignment(): AppResult<CategoryAssignment, CategoryAssignmentError> {
        val type = _uiState.value.type
        val name = _uiState.value.name

        if (type == null) {
            return AppResult.Failure(CategoryAssignmentError.TypeIsEmpty)
        }

        if (name == null || name.isEmpty()) {
            return AppResult.Failure(CategoryAssignmentError.NameIsEmpty)
        }

        val condition = _uiState.value.condition
        val categoryId = _uiState.value.categoryId
        val isRegex = _uiState.value.isRegex
        when (type) {
            CategoryAssignmentType.PRODUCT -> {
                return AppResult.Success(
                    CategoryAssignment.Product(
                        id = assignment?.id,
                        name = name,
                        condition = condition,
                        categoryId = categoryId,
                        regex = isRegex
                    )
                )
            }

            CategoryAssignmentType.STORE -> {
                return AppResult.Success(
                    CategoryAssignment.Store(
                        id = assignment?.id,
                        name = name,
                        condition = condition,
                        categoryId = categoryId,
                        regex = isRegex
                    )
                )
            }
        }
    }

    fun onSaveClick() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }

                when (val assignmentRet = createCategoryAssignment()) {
                    is AppResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                message = assignmentRet.error.message,
                                isLoading = false
                            )
                        }
                        return@launch
                    }

                    is AppResult.Success -> {
                        val assignment = assignmentRet.value
                        if (assignment.id == null) {
                            categoryAssignmentRepository.addCategoryAssignment(assignment)
                        } else {
                            categoryAssignmentRepository.updateCategoryAssignment(assignment)
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        isSaved = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}