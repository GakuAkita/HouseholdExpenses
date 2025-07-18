package gaku.original.myapplication.repository.RealtimeDBrepository

import addDataToRTDbWithId
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import gaku.original.myapplication.RealtimeDbReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.CategoryAssignmentData
import gaku.original.myapplication.data.mapFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import removeDataFromRTDb
import updateDataToRTDb
import javax.inject.Inject

class CategoryAssignmentRepository @Inject constructor(
    private val realtimeDbReference: RealtimeDbReference
) {
    private val className = this::class.simpleName ?: "UnableToGetClassName"

    suspend fun getCategoryAssignmentDataRef(): FetchResult<DatabaseReference> {
        return realtimeDbReference.getCategoryAssignmentDataRef()
    }

    suspend fun getProductNameCategoryAssignmentRef(): FetchResult<DatabaseReference> {
        return realtimeDbReference.getProductNameCategoryAssignmentRef()
    }

    suspend fun getStoreNameCategoryAssignmentRef(): FetchResult<DatabaseReference> {
        return realtimeDbReference.getStoreNameCategoryAssignmentRef()
    }

    suspend fun getCategoryAssignmentData(): FetchResult<CategoryAssignmentData> {
        val funcName = ::getCategoryAssignmentData.name

        val refRet = getCategoryAssignmentDataRef()
        if (refRet !is FetchResult.Success) {
            return refRet.mapFailure()
        }

        val ref = refRet.data

        return try {
            withTimeout(10000) {
                withContext(Dispatchers.IO) {
                    val snapshot = ref.get().await()
                    if (!snapshot.exists()) {
                        val result = FetchResult.Success(
                            data = CategoryAssignmentData(),
                        )
                        return@withContext result
                    }
                    val data = snapshot.getValue(CategoryAssignmentData::class.java)
                    if (data == null) {
                        val result = FetchResult.Failure.GenericFailure(
                            status = SuspendFuncStatus.FAILED,
                            errorMessage = "Unable to convert data to ${CategoryAssignmentData::class.simpleName}"
                        )
                        return@withContext result
                    } else {
                        val result = FetchResult.Success(
                            data = data
                        )
                        return@withContext result
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "${funcName} Timeout.")
            FetchResult.Failure.Timeout()
        } catch (e: Exception) {
            FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun getProductNameCategoryAssignment(): FetchResult<Map<String, CategoryAssignment>> {
        val refRet = getProductNameCategoryAssignmentRef()
        if (refRet !is FetchResult.Success) {
            return refRet.mapFailure()
        }
        val ref = refRet.data
        return getCategoryAssignments(ref)
    }

    suspend fun getStoreNameCategoryAssignment(): FetchResult<Map<String, CategoryAssignment>> {
        val refRet = getStoreNameCategoryAssignmentRef()
        if (refRet !is FetchResult.Success) {
            return refRet.mapFailure()
        }
        val ref = refRet.data
        return getCategoryAssignments(ref)
    }

    /* Productなのか、Storeなのかはreferenceだけが違う */
    suspend fun getCategoryAssignments(reference: DatabaseReference): FetchResult<Map<String, CategoryAssignment>> {
        val funcName = ::getCategoryAssignments.name
        return try {
            withTimeout(10000) {
                withContext(Dispatchers.IO) {
                    val snapshot = reference.get().await()
                    if (!snapshot.exists()) {
                        val result = FetchResult.Success<Map<String, CategoryAssignment>>(
                            data = emptyMap(),
                            isEmpty = true
                        )
                        return@withContext result
                    }
                    val typeIndicator =
                        object : GenericTypeIndicator<Map<String, CategoryAssignment>>() {}
                    val data = snapshot.getValue(typeIndicator)
                    if (data == null) {
                        val result = FetchResult.Failure.GenericFailure(
                            status = SuspendFuncStatus.FAILED,
                            errorMessage = "Unable to convert data to ${CategoryAssignmentData::class.simpleName}"
                        )
                        return@withContext result
                    } else {
                        val result = FetchResult.Success(
                            data = data
                        )
                        return@withContext result
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "${funcName} Timeout.")
            FetchResult.Failure.Timeout()
        } catch (e: Exception) {
            FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun addCategoryAssignment(
        categoryAssignment: CategoryAssignment,
        reference: DatabaseReference
    ): SuspendFuncStatusInfo {
        return addDataToRTDbWithId(
            data = categoryAssignment,
            reference = reference
        )
    }

    suspend fun updateCategoryAssignment(
        categoryAssignment: CategoryAssignment,
        reference: DatabaseReference//これは親を渡す
    ): SuspendFuncStatusInfo {
        return updateDataToRTDb(
            data = categoryAssignment,
            reference = reference
        )
    }

    suspend fun removeCategoryAssignment(
        categoryAssignment: CategoryAssignment,
        reference: DatabaseReference
    ): SuspendFuncStatusInfo {
        return removeDataFromRTDb(
            categoryAssignment,
            reference
        )
    }
}