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
    val assignments: List<CategoryAssignment> = listOf(),
    val categories: List<Category> = listOf()
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
            try {
                val categories = categoryRepository.categories.value
                _uiState.update {
                    it.copy(
                        categories = categories.values.toList()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = e.message
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
                        assignments = data.values.toList(),
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

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}