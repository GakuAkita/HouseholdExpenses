package gaku.original.myapplication.data.RealtimeDBrepository

import addDataToRTDb
import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.RealtimeDbReference
import gaku.original.myapplication.Utility.LogClassFuncCalled
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import removeDataFromRTDb
import updateDataToRTDb
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val realtimeDbReference: RealtimeDbReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    suspend fun getCategoryRef(callback: (SuspendFuncStatus) -> Unit): DatabaseReference? {
        return realtimeDbReference.getUserCategoryRef(callback)
    }

    suspend fun fetchAllCategories(
        callback: (SuspendFuncStatus) -> Unit
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
            withTimeout(3000) {
                val snapshot = categoryRef.get().await()
                val categories = snapshot.children.mapNotNull {
                    it.getValue(Category::class.java)

                }
                Log.d(className, "Fetched Categories: $categories")
                ret = categories
                callback(SuspendFuncStatus.SUCCESS)
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "${funcName} Timeout.")
            callback(SuspendFuncStatus.TIMEOUT)
        } catch (e: Exception) {
            Log.d(className, "${funcName} failed. ${e.message}")
            callback(SuspendFuncStatus.FAILED)
        }
        return ret  // エラー時には空のリストを返す
    }

    suspend fun addCategory(
        category: Category,
        callback: (SuspendFuncStatus) -> Unit
    ): SuspendFuncStatus {
        val funcName = ::addCategory.name
        var ret = SuspendFuncStatus.FAILED
        LogClassFuncCalled(className, funcName)
        val ref = getCategoryRef { status ->
            if (status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        }

        if (ref == null) {
            callback(SuspendFuncStatus.FAILED)
            return SuspendFuncStatus.FAILED
        }

        ret = addDataToRTDb(category, ref, callback)

        return ret
    }

    suspend fun updateCategory(
        category: Category,
        callback: (SuspendFuncStatus) -> Unit
    ): SuspendFuncStatus {
        val funcName = ::updateCategory.name
        LogClassFuncCalled(className, funcName)
        var ret = SuspendFuncStatus.FAILED
        val reference = getCategoryRef { status ->
            if (status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        }
        if (reference == null) {
            return ret
        }
        ret = updateDataToRTDb(category, reference, callback)

        return ret
    }

    suspend fun removeCategory(
        category: Category,
        callback: (SuspendFuncStatus) -> Unit
    ): SuspendFuncStatus {
        val funcName = ::removeCategory.name
        LogClassFuncCalled(className, funcName)
        var ret = SuspendFuncStatus.FAILED
        val reference = getCategoryRef { status ->
            if (status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        }
        if (reference == null) {
            return ret
        }

        ret = removeDataFromRTDb(category, reference, callback)

        return ret
    }
}