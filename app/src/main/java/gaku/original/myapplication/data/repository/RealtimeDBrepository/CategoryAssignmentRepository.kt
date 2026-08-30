package gaku.original.myapplication.data.repository.RealtimeDBrepository

import gaku.original.myapplication.RealtimeDbReference
import javax.inject.Inject

class CategoryAssignmentRepository @Inject constructor(
    private val realtimeDbReference: RealtimeDbReference
) {
//    private val className = this::class.simpleName ?: "UnableToGetClassName"
//
//    suspend fun getCategoryAssignmentDataRef(): FuncResultWithData<DatabaseReference> {
//        return realtimeDbReference.getCategoryAssignmentDataRef()
//    }
//
//    suspend fun getProductNameCategoryAssignmentRef(): FuncResultWithData<DatabaseReference> {
//        return realtimeDbReference.getProductNameCategoryAssignmentRef()
//    }
//
//    suspend fun getStoreNameCategoryAssignmentRef(): FuncResultWithData<DatabaseReference> {
//        return realtimeDbReference.getStoreNameCategoryAssignmentRef()
//    }
//
//    suspend fun getCategoryAssignmentData(
//        timeout: Long = 10000
//    ): FuncResultWithData<CategoryAssignmentData> {
//        val funcName = ::getCategoryAssignmentData.name
//
//        val refRet = getCategoryAssignmentDataRef()
//        if (refRet !is FuncResultWithData.Success) {
//            return refRet.mapFailure()
//        }
//
//        val ref = refRet.data
//
//        return try {
//            withTimeout(timeout) {
//                withContext(Dispatchers.IO) {
//                    val snapshot = ref.get().await()
//                    if (!snapshot.exists()) {
//                        val result = FuncResultWithData.Success(
//                            data = CategoryAssignmentData(),
//                        )
//                        return@withContext result
//                    }
//                    val data = snapshot.getValue(CategoryAssignmentData::class.java)
//                    if (data == null) {
//                        val result = FuncResultWithData.Failure.GenericFailure(
//                            status = FuncStatus.FAILED,
//                            errorMessage = "Unable to convert data to ${CategoryAssignmentData::class.simpleName}"
//                        )
//                        return@withContext result
//                    } else {
//                        val result = FuncResultWithData.Success(
//                            data = data
//                        )
//                        return@withContext result
//                    }
//                }
//            }
//        } catch (e: TimeoutCancellationException) {
//            Log.d(className, "${funcName} Timeout.")
//            FuncResultWithData.Failure.Timeout()
//        } catch (e: Exception) {
//            FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = e.message ?: "Unknown error"
//            )
//        }
//    }
//
//    suspend fun getProductNameCategoryAssignment(): FuncResultWithData<Map<String, CategoryAssignment>> {
//        val refRet = getProductNameCategoryAssignmentRef()
//        if (refRet !is FuncResultWithData.Success) {
//            return refRet.mapFailure()
//        }
//        val ref = refRet.data
//        return getCategoryAssignments(ref)
//    }
//
//    suspend fun getStoreNameCategoryAssignment(): FuncResultWithData<Map<String, CategoryAssignment>> {
//        val refRet = getStoreNameCategoryAssignmentRef()
//        if (refRet !is FuncResultWithData.Success) {
//            return refRet.mapFailure()
//        }
//        val ref = refRet.data
//        return getCategoryAssignments(ref)
//    }
//
//    /* Productなのか、Storeなのかはreferenceだけが違う */
//    suspend fun getCategoryAssignments(reference: DatabaseReference): FuncResultWithData<Map<String, CategoryAssignment>> {
//        val funcName = ::getCategoryAssignments.name
//        return try {
//            withTimeout(10000) {
//                withContext(Dispatchers.IO) {
//                    val snapshot = reference.get().await()
//                    if (!snapshot.exists()) {
//                        val result = FuncResultWithData.Success<Map<String, CategoryAssignment>>(
//                            data = emptyMap(),
//                            isEmpty = true
//                        )
//                        return@withContext result
//                    }
//                    val typeIndicator =
//                        object : GenericTypeIndicator<Map<String, CategoryAssignment>>() {}
//                    val data = snapshot.getValue(typeIndicator)
//                    if (data == null) {
//                        val result = FuncResultWithData.Failure.GenericFailure(
//                            status = FuncStatus.FAILED,
//                            errorMessage = "Unable to convert data to ${CategoryAssignmentData::class.simpleName}"
//                        )
//                        return@withContext result
//                    } else {
//                        val result = FuncResultWithData.Success(
//                            data = data
//                        )
//                        return@withContext result
//                    }
//                }
//            }
//        } catch (e: TimeoutCancellationException) {
//            Log.d(className, "${funcName} Timeout.")
//            FuncResultWithData.Failure.Timeout()
//        } catch (e: Exception) {
//            FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = e.message ?: "Unknown error"
//            )
//        }
//    }
//
//    suspend fun addCategoryAssignment(
//        categoryAssignment: CategoryAssignment,
//        reference: DatabaseReference
//    ): FuncResultWithData<CategoryAssignment> {
//        return addDataToRTDbWithId(
//            data = categoryAssignment,
//            reference = reference
//        )
//    }
//
//    suspend fun updateCategoryAssignment(
//        categoryAssignment: CategoryAssignment,
//        reference: DatabaseReference//これは親を渡す
//    ): FuncStatusInfo {
//        return updateDataToRTDb(
//            data = categoryAssignment,
//            reference = reference
//        )
//    }
//
//    suspend fun removeCategoryAssignment(
//        categoryAssignment: CategoryAssignment,
//        reference: DatabaseReference
//    ): FuncStatusInfo {
//        return removeDataFromRTDb(
//            categoryAssignment,
//            reference
//        )
//    }
}