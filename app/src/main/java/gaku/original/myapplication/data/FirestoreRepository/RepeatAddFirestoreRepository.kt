package gaku.original.myapplication.data.FirestoreRepository

import addDataWithIdToFirestore
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.Utility.LogClassFuncCalled
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.RepeatAdd
import gaku.original.myapplication.data.SuspendFuncStatusInfo
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

}