package gaku.original.myapplication.data.repository.RealtimeDBrepository
//import addDataToRTDbWithPush
//import android.util.Log
//import com.google.firebase.database.DatabaseReference
//import gaku.original.myapplication.RealtimeDbReference
//import gaku.original.myapplication.data.Constants.Status.FuncStatus
//import gaku.original.myapplication.data.FuncStatusInfo
//import gaku.original.myapplication.data.dataClass.Category
//import gaku.original.myapplication.utility.LogClassFuncCalled
//import kotlinx.coroutines.TimeoutCancellationException
//import kotlinx.coroutines.tasks.await
//import kotlinx.coroutines.withTimeout
//import removeDataFromRTDb
//import updateDataToRTDb
//import javax.inject.Inject
//
//class CategoryRepository @Inject constructor(
//    private val realtimeDbReference: RealtimeDbReference
//) {
//    private val className: String = this::class.simpleName ?: "UnableToGetClassName"
//
//    suspend fun getCategoryRef(callback: (FuncStatusInfo) -> Unit): DatabaseReference? {
//        return realtimeDbReference.getUserCategoryRef(callback)
//    }
//
//    suspend fun fetchAllCategories(
//        callback: (FuncStatusInfo) -> Unit
//    ): List<Category> {
//        val funcName: String = ::fetchAllCategories.name
//        var ret = emptyList<Category>()
//        LogClassFuncCalled(className, funcName)
//
//        val categoryRef = getCategoryRef { status ->
//            if (status.status != FuncStatus.SUCCESS) {
//                callback(status)
//            }
//        }
//        if (categoryRef == null) {
//            return ret
//        }
//
//        try {
//            withTimeout(3000) {
//                val snapshot = categoryRef.get().await()
//                val categories = snapshot.children.mapNotNull {
//                    it.getValue(Category::class.java)
//
//                }
//                Log.d(className, "Fetched Categories: $categories")
//                ret = categories
//                val statusInfo = FuncStatusInfo(
//                    status = FuncStatus.SUCCESS,
//                    errorMessage = ""
//                )
//                callback(statusInfo)
//            }
//        } catch (e: TimeoutCancellationException) {
//            Log.d(className, "${funcName} Timeout.")
//            val statusInfo = FuncStatusInfo(
//                status = FuncStatus.TIMEOUT,
//                errorMessage = "タイムアウトしました"
//            )
//            callback(statusInfo)
//        } catch (e: Exception) {
//            Log.d(className, "${funcName} failed. ${e.message}")
//            val statusInfo = FuncStatusInfo(
//                status = FuncStatus.FAILED,
//                errorMessage = e.message ?: "不明なエラー"
//            )
//            callback(statusInfo)
//        }
//        return ret  // エラー時には空のリストを返す
//    }
//
//    suspend fun addCategory(
//        category: Category,
//        callback: (FuncStatusInfo) -> Unit
//    ): FuncStatusInfo {
//        val funcName = ::addCategory.name
//        var ret = FuncStatusInfo(
//            status = FuncStatus.SUCCESS,
//            errorMessage = ""
//        )
//        LogClassFuncCalled(className, funcName)
//        val ref = getCategoryRef { status ->
//            if (status.status != FuncStatus.SUCCESS) {
//                ret = status
//                callback(status)
//            }
//        }
//
//        if (ref == null) {
//            return ret
//        }
//
//        ret = addDataToRTDbWithPush(category, ref, callback = callback)
//
//        return ret
//    }
//
//    suspend fun updateCategory(
//        category: Category,
//        callback: (FuncStatusInfo) -> Unit
//    ): FuncStatusInfo {
//        val funcName = ::updateCategory.name
//        LogClassFuncCalled(className, funcName)
//        var ret = FuncStatusInfo(
//            status = FuncStatus.SUCCESS,
//            errorMessage = ""
//        )
//        val reference = getCategoryRef { status ->
//            if (status.status != FuncStatus.SUCCESS) {
//                ret = status
//                callback(status)
//            }
//        }
//        if (reference == null) {
//            return ret
//        }
//        ret = updateDataToRTDb(category, reference, callback = callback)
//
//        return ret
//    }
//
//    suspend fun removeCategory(
//        category: Category,
//        callback: (FuncStatusInfo) -> Unit
//    ): FuncStatusInfo {
//        val funcName = ::removeCategory.name
//        LogClassFuncCalled(className, funcName)
//        var ret = FuncStatusInfo(
//            status = FuncStatus.SUCCESS,
//            errorMessage = ""
//        )
//        val reference = getCategoryRef { status ->
//            if (status.status != FuncStatus.SUCCESS) {
//                ret = status
//                callback(status)
//            }
//        }
//        if (reference == null) {
//            return ret
//        }
//
//        ret = removeDataFromRTDb(category, reference, callback = callback)
//        return ret
//    }
//}