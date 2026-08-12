package gaku.original.myapplication.data.repository.category

import gaku.original.myapplication.data.dataClass.Category

interface CategoryRepository {

    fun getAllCategories():Map<String, Category>

    fun addCategory(category: Category)

    fun updateCategory(category: Category)

    fun deleteCategory(categoryId:String)
}