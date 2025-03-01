package gaku.original.myapplication.data

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.RealtimeDbReference
import gaku.original.myapplication.data.RepositoryUtil.addDataToRTDb
import gaku.original.myapplication.data.RepositoryUtil.removeDataFromRTDb
import gaku.original.myapplication.data.RepositoryUtil.updateDataToRTDb
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val realtimeDbReference: RealtimeDbReference
) {
    val categoryRef: DatabaseReference
        get() = realtimeDbReference.getUserCategoryRef()

    suspend fun fetchAllCategories(
        callback: (Boolean) -> Unit = {}
    ): List<Category> {
        try {
            val snapshot = categoryRef.get().await()
            val categories = snapshot.children.mapNotNull {
                it.getValue(Category::class.java)
            }
            Log.d("CategoryRepository", "Fetched Categories: $categories")
            callback(true)
            return categories
        } catch (e: Exception) {
            Log.d("CategoryRepository", "fetchUserExpenses failed. ${e.message}")
            callback(false)
            return emptyList()  // エラー時には空のリストを返す
        }
    }

    fun addCategory(category: Category, callback: (Boolean) -> Unit = {}) {
        addDataToRTDb(category, { categoryRef }, callback)
    }

    fun updateCategory(category: Category, callback: (Boolean) -> Unit = {}) {
        updateDataToRTDb(category, { categoryRef }, callback)
    }

    fun removeCategory(category: Category, callback: (Boolean) -> Unit = {}) {
        removeDataFromRTDb(category, { categoryRef }, callback)
    }
}