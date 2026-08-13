package gaku.original.myapplication.ui.screens.global.categoryEdit

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
import gaku.original.myapplication.data.repository.category.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class CategoryEditUiState(
    val message: String? = null,
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val isShowEditDialog: Boolean = false,
    val isDeleteShowDialog: Boolean = false,
    val selectedCategory: Category? = null,
    val messageInDialog: String? = null
)

sealed interface CategoryInputError: AppError{
    data object EmptyName: CategoryInputError{
        override val message: String
            get() = "Category name is empty"
    }

    data object DuplicateName: CategoryInputError{
        override val message: String
            get() = "Category name is already used"
    }
}

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
//        val categories = categoryRepository.getAllCategories()
//        _uiState.value = _uiState.value.copy(
//            categories = categories.values.toList()
//        )

        viewModelScope.launch {
            categoryRepository.categories.collect {categories->
                _uiState.update {
                    it.copy(
                        categories = categories.values.toList()
                    )
                }
            }
        }
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

    fun onDeleteIconClick(category: Category) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                isDeleteShowDialog = true
            )
        }
    }

    private suspend fun validateCategory(category: Category): AppResult<Unit, CategoryInputError> {
        if(category.name == null ||
            category.name.isEmpty()){
            return AppResult.Failure(CategoryInputError.EmptyName)
        }

        val allCategories = categoryRepository.getAllCategories()
        if(allCategories.values.any{it.name == category.name}){
            return AppResult.Failure(CategoryInputError.DuplicateName)
        }
        return AppResult.Success(Unit)
    }

    fun onSave(category: Category) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }

                val validateRet = validateCategory(category)
                when(validateRet){
                    is AppResult.Failure ->{
                        _uiState.update {
                            it.copy(
                                message = validateRet.error.message
                            )
                        }
                        return@launch
                    }

                    is AppResult.Success->{
                        Timber.d("Category(${category.name}) validated.")
                    }
                }

                if (category.id == null) {
                    /* Add new category */
                    categoryRepository.addCategory(category)
                } else {
                    /* update category */
                    categoryRepository.updateCategory(category)
                }

                _uiState.update {
                    it.copy(
                        isShowEditDialog = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = e.message
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onDelete(category: Category) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }
                categoryRepository.deleteCategory(category.id!!)
                closeDeleteDialog()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = e.message
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun closeEditDialog() {
        _uiState.update {
            it.copy(
                selectedCategory = null,
                isShowEditDialog = false
            )
        }
    }

    fun closeDeleteDialog() {
        _uiState.update {
            it.copy(
                selectedCategory = null,
                isDeleteShowDialog = false
            )
        }
    }
}