package gaku.original.myapplication.repository.FirestoreRepository

import addDataWithIdToFirestore
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.utility.LogClassFuncCalled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import removeDataFromFirestore
import updateDataToFirestore
import javax.inject.Inject

class RepeatAddFirestoreRepository @Inject constructor(
    private val firestoreReference: FirestoreReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    fun getRepeatAddColRef(): CollectionReference? {
        return firestoreReference.getRepeatAddColRef()
    }

    suspend fun addRepeatAdd(
        repeatAdd: RepeatAdd,
    ): FuncResultWithData<RepeatAdd> {
        val funcName = ::addRepeatAdd.name
        LogClassFuncCalled(className, funcName)

        val ref = getRepeatAddColRef()
        if (ref == null) {
            val statusInfo = FuncResultWithData.Failure.GenericFailure(
                FuncStatus.FAILED,
                "RepeatAddコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = addDataWithIdToFirestore(repeatAdd, ref)
        return statusInfo
    }

    suspend fun updateRepeatAdd(
        repeatAdd: RepeatAdd,
    ): FuncStatusInfo {
        val ref = getRepeatAddColRef()
        if (ref == null) {
            val statusInfo = FuncStatusInfo(
                FuncStatus.FAILED,
                "RepeatAddコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = updateDataToFirestore(repeatAdd, ref)
        return statusInfo
    }

    suspend fun removeRepeatAdd(
        repeatAdd: RepeatAdd,
    ): FuncStatusInfo {
        val ref = getRepeatAddColRef()
        if (ref == null) {
            val statusInfo = FuncStatusInfo(
                FuncStatus.FAILED,
                "RepeatAddコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = removeDataFromFirestore(repeatAdd, ref)
        return statusInfo
    }

    suspend fun fetchAllRepeatAdd(
        timeout: Long = 10000
    ): FuncResultWithData<List<RepeatAdd>> {
        val funcName = ::fetchAllRepeatAdd.name
        LogClassFuncCalled(className, funcName)

        val repeatAddRef = getRepeatAddColRef()

        if (repeatAddRef == null) {
            val result = FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "RepeatAddコレクションが参照できませんでした"
            )
            return result
        }

        return try {
            withTimeout(timeout) {
                withContext(Dispatchers.IO) {
                    val snapshot = repeatAddRef.get().await()

                    val list = mutableListOf<RepeatAdd>()
                    for (doc in snapshot.documents) {
                        val repeatAdd = doc.toObject(RepeatAdd::class.java)
                            ?: throw Exception("Categoryへの変換に失敗 docId=${doc.id}")
                        list.add(repeatAdd)
                    }

                    Log.d(className, "Fetched Categories: $list")
                    val result = FuncResultWithData.Success(
                        data = list
                    )
                    /* 戻り値 */
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
                errorMessage = e.message ?: "不明なエラー"
            )
            result
        }
    }

}