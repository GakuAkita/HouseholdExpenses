package gaku.original.myapplication.ui.screens.global.categoryEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.repository.category.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

data class CategoryEditUiState(
    val message: String? = null,
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val isShowEditDialog: Boolean = false,
    val isDeleteShowDialog: Boolean = false,
    val selectedCategory: Category? = null
)

class CategoryEditViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryEditUiState())
    val uiState get() = _uiState

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY]) as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                CategoryEditViewModel(
                    categoryRepository = session.categoryRepository
                )
            }
        }
    }

    init {
        val categories = categoryRepository.getAllCategories()
        _uiState.value = _uiState.value.copy(
            categories = categories.values.toList()
        )
    }

    fun onCategorySelected(category: Category) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                isShowEditDialog = true
            )
        }
    }

    fun onCategoryAddClick() {
        _uiState.update {
            it.copy(
                selectedCategory = Category(id = null, name = null),
                isShowEditDialog = true
            )
        }
    }

    fun closeEditDialog() {
        _uiState.update {
            it.copy(
                isShowEditDialog = false
            )
        }
    }

    fun closeDeleteDialog() {
        _uiState.update {
            it.copy(
                isDeleteShowDialog = false
            )
        }
    }
}