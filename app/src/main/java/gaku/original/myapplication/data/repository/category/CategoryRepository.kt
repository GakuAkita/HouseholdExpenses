package gaku.original.myapplication.data.repository.category

import gaku.original.myapplication.data.dataClass.Category

interface CategoryRepository {

    fun getAllCategories():Map<String, Category>

    fun saveCategory(category: Category)
}