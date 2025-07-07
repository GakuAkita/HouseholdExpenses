package gaku.original.myapplication.data.Repository.FirestoreRepository

import addDataWithIdToFirestore
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.SuspendFuncStatusInfo
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
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): SuspendFuncStatusInfo {
        val funcName = ::addRepeatAdd.name
        LogClassFuncCalled(className, funcName)

        val ref = getRepeatAddColRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "RepeatAddコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = addDataWithIdToFirestore(repeatAdd, ref, callback = callback)
        return statusInfo
    }

    suspend fun updateRepeatAdd(
        repeatAdd: RepeatAdd,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val ref = getRepeatAddColRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "RepeatAddコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = updateDataToFirestore(repeatAdd, ref, callback = callback)
        return statusInfo
    }

    suspend fun removeRepeatAdd(
        repeatAdd: RepeatAdd,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val ref = getRepeatAddColRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "RepeatAddコレクションが参照できませんでした"
            )
            return statusInfo
        }

        val statusInfo = removeDataFromFirestore(repeatAdd, ref, callback = callback)
        return statusInfo
    }

    suspend fun fetchAllRepeatAdd(
        timeout: Long = 10000,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): FetchResult<List<RepeatAdd>> {
        val funcName = ::fetchAllRepeatAdd.name
        LogClassFuncCalled(className, funcName)

        val repeatAddRef = getRepeatAddColRef()

        if (repeatAddRef == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "RepeatAddコレクションが参照できませんでした"
            )

            callback(statusInfo)

            return FetchResult(statusInfo.status, statusInfo.errorMessage)
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
                    val statusInfo = SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, "")
                    callback(statusInfo)

                    /* 戻り値 */
                    FetchResult(statusInfo.status, statusInfo.errorMessage, list)
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "$funcName Timeout.")
            val statusInfo =
                SuspendFuncStatusInfo(SuspendFuncStatus.TIMEOUT, "タイムアウトしました")
            callback(statusInfo)

            FetchResult(statusInfo.status, statusInfo.errorMessage)
        } catch (e: Exception) {
            Log.d(className, "$funcName failed. ${e.message}")
            val statusInfo =
                SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, e.message ?: "不明なエラー")
            callback(statusInfo)

            FetchResult(statusInfo.status, statusInfo.errorMessage)
        }
    }

}