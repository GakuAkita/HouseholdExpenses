package gaku.original.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryEditViewModel @Inject constructor(
    private val expenseSharedViewModel: ExpenseSharedViewModel
) : ViewModel() {

    val allCategories: StateFlow<List<Category>> get() = expenseSharedViewModel.allCategories

    fun addCategory(
        category: Category,
        onDuplicateCategory: () -> Unit,
        callback: (SuspendFuncStatus) -> Unit = {}
    ) {
        viewModelScope.launch {
            expenseSharedViewModel.addCategory(category, onDuplicateCategory, callback)
        }
    }

    fun updateCategory(
        category: Category,
        onDuplicateCategory: () -> Unit,
        callback: (SuspendFuncStatus) -> Unit = {}
    ) {
        viewModelScope.launch {
            expenseSharedViewModel.updateCategory(category, onDuplicateCategory, callback)
        }
    }

    fun removeCategory(category: Category, callback: (SuspendFuncStatus) -> Unit = {}) {
        viewModelScope.launch {
            expenseSharedViewModel.removeCategory(category, callback)
        }
    }
}