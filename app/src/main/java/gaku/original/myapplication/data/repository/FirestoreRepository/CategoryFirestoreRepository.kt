package gaku.original.myapplication.data.repository.FirestoreRepository

import addDataWithIdToFirestore
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.utility.LogAkitaDebug
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
    ): FuncResultWithData<Category> {
        val ref = getCategoriesColRef()
        if (ref == null) {
            val statusInfo = FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "Categoriesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = addDataWithIdToFirestore(category, ref)
        return statusInfo
    }

    suspend fun updateCategory(
        category: Category,
    ): FuncStatusInfo {
        val ref = getCategoriesColRef()
        if (ref == null) {
            val statusInfo = FuncStatusInfo(
                FuncStatus.FAILED,
                "Categoriesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = updateDataToFirestore(category, ref)
        return statusInfo
    }

    suspend fun removeCategory(
        category: Category
    ): FuncStatusInfo {
        val ref = getCategoriesColRef()
        if (ref == null) {
            val statusInfo = FuncStatusInfo(
                FuncStatus.FAILED,
                "Expensesコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = removeDataFromFirestore(category, ref)
        return statusInfo
    }

    suspend fun fetchAllCategories(
        timeout: Long = 10000,
    ): FuncResultWithData<List<Category>> {
        LogAkitaDebug("fetchAllCategories called. timeout=$timeout Intentional")
//        return FuncResultWithData.Failure.Timeout(
//            errorMessage = "fetchAllCategories() timeout.Intentional"
//        )
        val funcName = ::fetchAllCategories.name
        LogClassFuncCalled(className, funcName)

        val categoryRef = getCategoriesColRef()

        if (categoryRef == null) {
            val result = FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
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
                    val result = FuncResultWithData.Success(
                        data = list
                    )
                    result
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "$funcName Timeout.")
            val result = FuncResultWithData.Failure.Timeout()
            result
        } catch (e: Exception) {
            Log.d(className, "$funcName failed. ${e.message}")
            val result = FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "${e.message}"
            )
            result
        }
    }
}