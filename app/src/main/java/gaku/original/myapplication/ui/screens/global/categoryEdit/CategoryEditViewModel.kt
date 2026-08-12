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

data class CategoryEditUiState(
    val message: String? = null,
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList()
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

    }
}