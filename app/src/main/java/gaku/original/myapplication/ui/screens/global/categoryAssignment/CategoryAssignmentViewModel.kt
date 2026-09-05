package gaku.original.myapplication.ui.screens.global.categoryAssignment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.categoryAssignment.CategoryAssignmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class CategoryAssignmentUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val assignments: List<AssignmentUiState<CategoryAssignment>> = listOf(),
    val categories: List<Category> = listOf()
)

data class AssignmentUiState<out T : CategoryAssignment>(
    val isLoading: Boolean = false,
    val assignment: T,
)

class CategoryAssignmentViewModel(
    private val categoryRepository: CategoryRepository,
    private val categoryAssignmentRepository: CategoryAssignmentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoryAssignmentUiState())
    val uiState = _uiState.asStateFlow()

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val appContainer = app.appContainer
                val session = appContainer.sessionContainer!!
                CategoryAssignmentViewModel(
                    session.categoryRepository,
                    session.categoryAssignmentRepository
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")

        viewModelScope.launch {
            categoryRepository.categories.collect { categories ->
                _uiState.update {
                    it.copy(
                        categories = categories.values.toList()
                    )
                }
            }
        }

        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }
                val data = categoryAssignmentRepository.getCategoryAssignments()
                _uiState.update {
                    it.copy(
                        assignments = data.values.map {
                            AssignmentUiState(
                                isLoading = false,
                                assignment = it
                            )
                        },
                        isLoading = false
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

    fun onMessageShown() {
        _uiState.update {
            it.copy(
                message = null
            )
        }
    }

    fun onCategorySelected(
        assignmentUiState: AssignmentUiState<CategoryAssignment>,
        categoryId: String?
    ) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        assignments = it.assignments.map {
                            if (it.assignment.id == assignmentUiState.assignment.id) {
                                it.copy(
                                    isLoading = true
                                )
                            } else {
                                it
                            }
                        }
                    )
                }
                val assignment = assignmentUiState.assignment
                var newAssignment: CategoryAssignment
                when (assignment) {
                    is CategoryAssignment.Product -> {
                        categoryAssignmentRepository.updateCategoryAssignment(
                            assignment.copy(categoryId = categoryId)
                        )
                        newAssignment = assignment.copy(categoryId = categoryId)
                    }

                    is CategoryAssignment.Store -> {
                        categoryAssignmentRepository.updateCategoryAssignment(
                            assignment.copy(categoryId = categoryId)
                        )
                        newAssignment = assignment.copy(categoryId = categoryId)
                    }
                }
                _uiState.update {
                    it.copy(
                        assignments = it.assignments.map {
                            if (it.assignment.id == assignmentUiState.assignment.id) {
                                it.copy(
                                    isLoading = false,
                                    assignment = newAssignment
                                )
                            } else {
                                it
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = e.message,
                        assignments = it.assignments.map {
                            if (it.assignment.id == assignmentUiState.assignment.id) {
                                it.copy(
                                    isLoading = false
                                )
                            } else {
                                it
                            }
                        }
                    )
                }
            }
        }
    }

    fun onDeleteClick(categoryAssignment: CategoryAssignment) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        assignments = it.assignments.map {
                            if (it.assignment.id == categoryAssignment.id) {
                                it.copy(
                                    isLoading = true
                                )
                            } else {
                                it
                            }
                        }
                    )
                }
                categoryAssignmentRepository.deleteCategoryAssignment(categoryAssignment)
                _uiState.update {
                    it.copy(
                        assignments = it.assignments.filter {
                            it.assignment.id != categoryAssignment.id
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = e.message,
                        assignments = it.assignments.map {
                            if (it.assignment.id == categoryAssignment.id) {
                                it.copy(
                                    isLoading = false
                                )
                            } else {
                                it
                            }
                        }
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