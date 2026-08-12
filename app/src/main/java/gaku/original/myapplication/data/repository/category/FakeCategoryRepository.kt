package gaku.original.myapplication.data.repository.category

import gaku.original.myapplication.data.dataClass.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import kotlin.uuid.Uuid

class FakeCategoryRepository : CategoryRepository {

    val sampleCategories = mapOf(
        "1" to Category(
            id = "1",
            name = "食費"
        ),
        "2" to Category(
            id = "2",
            name = "交通費"
        ),
        "3" to Category(
            id = "3",
            name = "交通費2"
        ),
        "4" to Category(
            id = "4",
            name = "交通費4"
        ),
        "5" to Category(
            id = "5",
            name = "交通費5"
        ),
        "6" to Category(
            id = "6",
            name = "交通費6"
        ),
    )

    private val _categories = MutableStateFlow<Map<String,Category>>(emptyMap())
    override val categories: StateFlow<Map<String, Category>> get() = _categories

    init{
        _categories.value = sampleCategories
    }

    override fun getAllCategories(): Map<String, Category> {
        return _categories.value
    }

    override fun addCategory(category: Category) {
        val newCategory = category.copy(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis()
        )
        _categories.value += (newCategory.id!! to newCategory)
    }

    override fun updateCategory(category: Category) {
        _categories.value += (category.id!! to category)
    }

    override fun deleteCategory(categoryId: String) {
        _categories.value -= categoryId
    }
}