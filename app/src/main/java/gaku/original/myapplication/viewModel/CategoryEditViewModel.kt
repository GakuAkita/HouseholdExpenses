package gaku.original.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.SuspendFuncStatusInfo
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
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ) {
        viewModelScope.launch {
            expenseSharedViewModel.addCategory(category, callback)
        }
    }

    fun updateCategory(
        category: Category,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ) {
        viewModelScope.launch {
            expenseSharedViewModel.updateCategory(category, callback)
        }
    }

    fun removeCategory(category: Category, callback: (SuspendFuncStatusInfo) -> Unit = {}) {
        viewModelScope.launch {
            expenseSharedViewModel.removeCategory(category, callback)
        }
    }
}