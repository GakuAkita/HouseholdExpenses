package gaku.original.myapplication.data.repository.category

import gaku.original.myapplication.data.dataClass.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CategoryRepositoryFirestore : CategoryRepository {

    private val _categories = MutableStateFlow<Map<String, Category>>(emptyMap())
    override val categories: StateFlow<Map<String, Category>>
        get() = _categories

    init {
        /**
        startListening{

        }*/
    }

    override suspend fun addCategory(category: Category) {
        TODO("Not yet implemented")
    }

    override suspend fun updateCategory(category: Category) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCategory(categoryId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllCategories(): Map<String, Category> {
        TODO("Not yet implemented")
    }

    override fun close() {
        /* stopListening() */
    }
}