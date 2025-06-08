package gaku.original.myapplication.data.RealtimeDBrepository

import addDataToRTDb
import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.RealtimeDbReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.RepeatAdd
import gaku.original.myapplication.utility.LogClassFuncCalled
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import removeDataFromRTDb
import updateDataToRTDb
import javax.inject.Inject

class RepeatAddRepository @Inject constructor(
    private val realtimeDbReference: RealtimeDbReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    suspend fun getRepeatAddRef(callback: (SuspendFuncStatus) -> Unit = {}): DatabaseReference? {
        return realtimeDbReference.getUserRepeatAddRef(callback)
    }

    // ユーザーIDに基づいてデータをリストとして返す（非同期）
    suspend fun fetchRepeatAddSettings(
        callback: (SuspendFuncStatus) -> Unit = {}
    ): List<RepeatAdd> {
        val funcName = ::fetchRepeatAddSettings.name
        var ret = emptyList<RepeatAdd>()
        LogClassFuncCalled(className, funcName)
        val repeatAddRef = getRepeatAddRef { status ->
            if (status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        }

        if (repeatAddRef == null) {
            return ret
        }

        try {
            withTimeout(3000) {
                val snapshot = repeatAddRef.get().await()
                val repeatAdds = snapshot.children.mapNotNull {
                    it.getValue(RepeatAdd::class.java)
                }
                Log.d(className, "Fetched RepeatAdd: $repeatAdds")
                ret = repeatAdds
                callback(SuspendFuncStatus.SUCCESS)
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "${funcName} Timeout.")
            callback(SuspendFuncStatus.TIMEOUT)
        } catch (e: Exception) {
            Log.d(className, "${funcName} failed. ${e.message}")
            callback(SuspendFuncStatus.FAILED)
        }
        return ret
    }

    suspend fun addRepeatAdd(
        repeatAdd: RepeatAdd,
        callback: (SuspendFuncStatus) -> Unit = {}
    ): SuspendFuncStatus {
        val funcName = ::addRepeatAdd.name
        LogClassFuncCalled(className, funcName)
        var ret = SuspendFuncStatus.FAILED
        val reference = getRepeatAddRef() { status ->
            if (status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        }
        if (reference == null) {
            return ret
        }

        ret = addDataToRTDb(repeatAdd, reference, callback = callback)
        return ret
    }

    suspend fun updateRepeatAdd(
        repeatAdd: RepeatAdd,
        callback: (SuspendFuncStatus) -> Unit = {}
    ): SuspendFuncStatus {
        val funcName = ::updateRepeatAdd.name
        LogClassFuncCalled(className, funcName)
        var ret = SuspendFuncStatus.FAILED
        val reference = getRepeatAddRef { status ->
            if (status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        }
        if (reference == null) {
            return ret
        }
        ret = updateDataToRTDb(repeatAdd, reference, callback = callback)

        return ret
    }

    suspend fun removeRepeatAdd(
        repeatAdd: RepeatAdd,
        callback: (SuspendFuncStatus) -> Unit = {}
    ): SuspendFuncStatus {
        val funcName = ::removeRepeatAdd.name
        LogClassFuncCalled(className, funcName)
        var ret = SuspendFuncStatus.FAILED
        val reference = getRepeatAddRef() { status ->
            if (status != SuspendFuncStatus.SUCCESS) {
                callback(status)
            }
        }
        if (reference == null) {
            return ret
        }
        ret = removeDataFromRTDb(repeatAdd, reference, callback = callback)

        return ret
    }
}