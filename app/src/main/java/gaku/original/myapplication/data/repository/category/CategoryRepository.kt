package gaku.original.myapplication.data.repository.category

import gaku.original.myapplication.data.dataClass.Category
import kotlinx.coroutines.flow.StateFlow

interface CategoryRepository {

    val categories: StateFlow<Map<String, Category>>
    suspend fun getAllCategories():Map<String, Category>

    suspend fun addCategory(category: Category)

    suspend fun updateCategory(category: Category)

    suspend fun deleteCategory(categoryId:String)
}