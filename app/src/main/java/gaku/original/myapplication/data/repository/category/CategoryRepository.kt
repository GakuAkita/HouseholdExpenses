package gaku.original.myapplication.data.repository.category

import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.repository.ClosableRepository
import kotlinx.coroutines.flow.StateFlow

/* There are multiple ways to implement. */
/* When we just focus on Firebase, startListening and stopListening are necessary, but I'm not sure if it's true in other services... */
interface CategoryRepository : ClosableRepository {

    val categories: StateFlow<Map<String, Category>>
    suspend fun getAllCategories(): Map<String, Category>

    suspend fun addCategory(category: Category)

    suspend fun updateCategory(category: Category)

    suspend fun deleteCategory(categoryId: String)
}