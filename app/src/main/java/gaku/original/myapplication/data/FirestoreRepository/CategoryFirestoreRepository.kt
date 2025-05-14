package gaku.original.myapplication.data.FirestoreRepository

import addDataWithIdToFirestore
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.Utility.LogClassFuncCalled
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import removeDataFromFirestore
import updateDataToFirestore

class CategoryFirestoreRepository(
    private val firestoreReference: FirestoreReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    fun getCategoriesColRef(): CollectionReference? {
        return firestoreReference.getCategoriesColRef()
    }

    suspend fun addCategory(
        category: Category,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val ref = getCategoriesColRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Categoriesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        /* タイムアウトは設定しない */
        val statusInfo = addDataWithIdToFirestore(category, ref, callback = callback)
        return statusInfo
    }

    suspend fun updateCategory(
        category: Category,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val ref = getCategoriesColRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Expensesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = updateDataToFirestore(category, ref, callback = callback)
        return statusInfo
    }

    suspend fun removeCategory(
        category: Category,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val ref = getCategoriesColRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Expensesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = removeDataFromFirestore(category, ref, callback = callback)
        return statusInfo
    }

    suspend fun fetchAllCategories(
        timeout: Long = 10000,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): List<Category> {
        val funcName = ::fetchAllCategories.name
        var ret = emptyList<Category>()
        LogClassFuncCalled(className, funcName)

        val categoryRef = getCategoriesColRef()

        if (categoryRef == null) {
            callback(
                SuspendFuncStatusInfo(
                    SuspendFuncStatus.FAILED,
                    "Expensesコレクションが参照できませんでした"
                )
            )
            return ret
        }

        try {
            withTimeout(timeout) {
                val snapshot = categoryRef.get().await()

                val list = mutableListOf<Category>()
                for (doc in snapshot.documents) {
                    val expense = doc.toObject(Category::class.java)
                        ?: throw Exception("Categoryへの変換に失敗 docId=${doc.id}")
                    list.add(expense)
                }
                ret = list

                Log.d(className, "Fetched Categories: $ret")
                callback(SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, ""))
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "$funcName Timeout.")
            val statusInfo =
                SuspendFuncStatusInfo(SuspendFuncStatus.TIMEOUT, "タイムアウトしました")
            callback(statusInfo)
            ret = emptyList()
        } catch (e: Exception) {
            Log.d(className, "$funcName failed. ${e.message}")
            val statusInfo =
                SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, e.message ?: "不明なエラー")
            callback(statusInfo)
            ret = emptyList()
        }

        return ret
    }
}