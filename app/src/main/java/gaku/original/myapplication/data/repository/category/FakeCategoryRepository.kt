package gaku.original.myapplication.data.repository.category

import gaku.original.myapplication.data.dataClass.Category

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

    override fun getAllCategories(): Map<String, Category> {
        return sampleCategories
    }

    override fun saveCategory(category: Category) {

    }

    override fun updateCategory(category: Category) {
        TODO("Not yet implemented")
    }

    override fun deleteCategory(categoryId: String) {
        TODO("Not yet implemented")
    }
}