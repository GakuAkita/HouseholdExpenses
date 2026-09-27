package gaku.original.myapplication.data.repository.category

import gaku.original.myapplication.data.dataClass.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeCategoryRepository : CategoryRepository {

    val sampleCategories = mapOf(
        "1" to Category(
            id = "1",
            timestamp = 0L,
            name = "食費",
            enabled = true
        ),
        "2" to Category(
            id = "2",
            timestamp = 0L,
            name = "交通費",
            enabled = true
        ),
        "3" to Category(
            id = "3",
            timestamp = 0L,
            name = "交通費2",
            enabled = true
        ),
        "4" to Category(
            id = "4",
            timestamp = 0L,
            name = "交通費4",
            enabled = true
        ),
        "5" to Category(
            id = "5",
            timestamp = 0L,
            name = "交通費5",
            enabled = true
        ),
        "6" to Category(
            id = "6",
            timestamp = 0L,
            name = "交通費6",
            enabled = true
        ),
    )

    private val _categories = MutableStateFlow<Map<String, Category>>(emptyMap())
    override val categories: StateFlow<Map<String, Category>> get() = _categories

    init {
        _categories.value = sampleCategories
    }

    override suspend fun getAllCategories(): Map<String, Category> {
        return _categories.value
    }

    override suspend fun addCategory(category: Category): Category {
        _categories.value += (category.id to category)
        return category
    }

    override suspend fun updateCategory(category: Category) {
        _categories.value += (category.id to category)
    }

    override suspend fun deleteCategory(categoryId: String) {
        _categories.value -= categoryId
    }

    override fun close() {
        /* Do nothing */
    }
}