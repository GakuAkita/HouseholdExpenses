package gaku.original.myapplication.data.repository.category

import gaku.original.myapplication.data.dataClass.Category
import kotlinx.coroutines.flow.StateFlow

interface CategoryRepository {

    val categories: StateFlow<Map<String, Category>>
    fun getAllCategories():Map<String, Category>

    fun addCategory(category: Category)

    fun updateCategory(category: Category)

    fun deleteCategory(categoryId:String)
}