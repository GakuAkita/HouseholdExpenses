package gaku.original.myapplication.viewModel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.viewModel.ExpenseSharedViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryEditViewModel @Inject constructor(
    private val expenseSharedViewModel: ExpenseSharedViewModel,
) : ViewModel() {

    val allCategories: StateFlow<List<Category>> get() = expenseSharedViewModel.allCategories

    fun addCategory(
        category: Category,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ) {
        viewModelScope.launch {
            val ret = expenseSharedViewModel.addCategory(category)
            callback(ret)
        }
    }

    fun updateCategory(
        category: Category,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ) {
        /**
         * 繰り返し追加に存在していたらそこも更新する
         */
        viewModelScope.launch {
            val ret = expenseSharedViewModel.updateCategory(category)
            callback(ret)
        }
    }


    fun removeCategory(category: Category, callback: (SuspendFuncStatusInfo) -> Unit = {}) {
        viewModelScope.launch {
            /* 内部に繰り返し追加に入っていないかチェックしている */
            val ret = expenseSharedViewModel.removeCategory(category)
            callback(ret)
        }
    }
}