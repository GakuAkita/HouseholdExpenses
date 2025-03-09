package gaku.original.myapplication.data

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.RealtimeDbReference
import gaku.original.myapplication.Utility.LogClassFuncCalled
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.RepositoryUtil.addDataToRTDb
import gaku.original.myapplication.data.RepositoryUtil.removeDataFromRTDb
import gaku.original.myapplication.data.RepositoryUtil.updateDataToRTDb
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val realtimeDbReference: RealtimeDbReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    suspend fun getCategoryRef(callback: (SuspendFuncStatus) -> Unit = {}): DatabaseReference? {
        return realtimeDbReference.getUserCategoryRef(callback)
    }

    suspend fun fetchAllCategories(
        callback: (SuspendFuncStatus) -> Unit = {}
    ): List<Category> {
        val funcName: String = ::fetchAllCategories.name
        var ret = emptyList<Category>()
        LogClassFuncCalled(className, funcName)

        val categoryRef = getCategoryRef { status ->
            if (status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        }
        if (categoryRef == null) {
            return ret
        }

        try {
            val snapshot = getCategoryRef()
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
        addDataToRTDb(category, categoryRef, callback)
    }

    fun updateCategory(category: Category, callback: (Boolean) -> Unit = {}) {
        updateDataToRTDb(category, { categoryRef }, callback)
    }

    fun removeCategory(category: Category, callback: (Boolean) -> Unit = {}) {
        removeDataFromRTDb(category, { categoryRef }, callback)
    }
}