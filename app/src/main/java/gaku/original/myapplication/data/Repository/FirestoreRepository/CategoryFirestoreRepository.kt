package gaku.original.myapplication.data.Repository.FirestoreRepository

import addDataWithIdToFirestore
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.utility.LogClassFuncCalled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import removeDataFromFirestore
import updateDataToFirestore
import javax.inject.Inject

class CategoryFirestoreRepository @Inject constructor(
    private val firestoreReference: FirestoreReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    fun getCategoriesColRef(): CollectionReference? {
        return firestoreReference.getCategoriesColRef()
    }

    suspend fun addCategory(
        category: Category
    ): SuspendFuncStatusInfo {
        val ref = getCategoriesColRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Categoriesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = addDataWithIdToFirestore(category, ref)
        return statusInfo
    }

    suspend fun updateCategory(
        category: Category,
    ): SuspendFuncStatusInfo {
        val ref = getCategoriesColRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Categoriesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = updateDataToFirestore(category, ref)
        return statusInfo
    }

    suspend fun removeCategory(
        category: Category
    ): SuspendFuncStatusInfo {
        val ref = getCategoriesColRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Expensesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = removeDataFromFirestore(category, ref)
        return statusInfo
    }

    suspend fun fetchAllCategories(
        timeout: Long = 10000,
    ): FetchResult<List<Category>> {
        val funcName = ::fetchAllCategories.name
        LogClassFuncCalled(className, funcName)

        val categoryRef = getCategoriesColRef()

        if (categoryRef == null) {
            val result = FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "Categoriesコレクションが参照できませんでした"
            )
            return result
        }

        return try {
            withTimeout(timeout) {
                withContext(Dispatchers.IO) {
                    val snapshot = categoryRef.get().await()

                    val list = mutableListOf<Category>()
                    for (doc in snapshot.documents) {
                        val category = doc.toObject(Category::class.java)
                            ?: throw Exception("Categoryへの変換に失敗 docId=${doc.id}")
                        list.add(category)
                    }

                    Log.d(className, "Fetched Categories: $list")
                    val result = FetchResult.Success(
                        data = list
                    )
                    result
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "$funcName Timeout.")
            val result = FetchResult.Failure.Timeout()
            result
        } catch (e: Exception) {
            Log.d(className, "$funcName failed. ${e.message}")
            val result = FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "${e.message}"
            )
            result
        }
    }
}