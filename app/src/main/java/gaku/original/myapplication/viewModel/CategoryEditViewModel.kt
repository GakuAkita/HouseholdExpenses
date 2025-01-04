package gaku.original.myapplication.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.Status.CategoryEditStatus
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CategoryEditViewModel @Inject constructor(
    private val expenseSharedViewModel:ExpenseSharedViewModel
) :ViewModel(){

    val allCategories : StateFlow<List<Category>> get() = expenseSharedViewModel.allCategories

    fun addCategory(category: Category,callback:(CategoryEditStatus)->Unit ={}){
        expenseSharedViewModel.addCategory(category,callback)
    }

    fun updateCategory(category: Category,callback:(CategoryEditStatus)->Unit ={}){
        expenseSharedViewModel.updateCategory(category,callback)
    }

    fun removeCategory(category: Category,callback:(Boolean)->Unit ={}){
        expenseSharedViewModel.removeCategory(category,callback)
    }

}