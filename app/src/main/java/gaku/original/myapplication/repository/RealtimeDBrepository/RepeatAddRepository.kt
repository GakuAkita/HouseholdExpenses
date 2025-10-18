package gaku.original.myapplication.repository.RealtimeDBrepository
//
//import addDataToRTDbWithPush
//import android.util.Log
//import com.google.firebase.database.DatabaseReference
//import gaku.original.myapplication.RealtimeDbReference
//import gaku.original.myapplication.data.Constants.Status.FuncStatus
//import gaku.original.myapplication.data.FuncStatusInfo
//import gaku.original.myapplication.data.dataClass.RepeatAdd
//import gaku.original.myapplication.utility.LogClassFuncCalled
//import kotlinx.coroutines.TimeoutCancellationException
//import kotlinx.coroutines.tasks.await
//import kotlinx.coroutines.withTimeout
//import removeDataFromRTDb
//import updateDataToRTDb
//import javax.inject.Inject
//
//class RepeatAddRepository @Inject constructor(
//    private val realtimeDbReference: RealtimeDbReference
//) {
//    private val className: String = this::class.simpleName ?: "UnableToGetClassName"
//
//    suspend fun getRepeatAddRef(callback: (FuncStatusInfo) -> Unit = {}): DatabaseReference? {
//        return realtimeDbReference.getUserRepeatAddRef(callback)
//    }
//
//    // ユーザーIDに基づいてデータをリストとして返す（非同期）
//    suspend fun fetchRepeatAddSettings(
//        callback: (FuncStatusInfo) -> Unit = {}
//    ): List<RepeatAdd> {
//        val funcName = ::fetchRepeatAddSettings.name
//        var ret = emptyList<RepeatAdd>()
//        LogClassFuncCalled(className, funcName)
//        val repeatAddRef = getRepeatAddRef { status ->
//            if (status.status != FuncStatus.SUCCESS) {
//                callback(status)
//            }
//        }
//
//        if (repeatAddRef == null) {
//            return ret
//        }
//
//        try {
//            withTimeout(3000) {
//                val snapshot = repeatAddRef.get().await()
//                val repeatAdds = snapshot.children.mapNotNull {
//                    it.getValue(RepeatAdd::class.java)
//                }
//                Log.d(className, "Fetched RepeatAdd: $repeatAdds")
//                ret = repeatAdds
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
//                errorMessage = "Timeout occurred"
//            )
//            callback(statusInfo)
//        } catch (e: Exception) {
//            Log.d(className, "${funcName} failed. ${e.message}")
//            val statusInfo = FuncStatusInfo(
//                status = FuncStatus.FAILED,
//                errorMessage = e.message ?: "Unknown error"
//            )
//            callback(statusInfo)
//        }
//        return ret
//    }
//
//    suspend fun addRepeatAdd(
//        repeatAdd: RepeatAdd,
//        callback: (FuncStatusInfo) -> Unit = {}
//    ): FuncStatusInfo {
//        val funcName = ::addRepeatAdd.name
//        LogClassFuncCalled(className, funcName)
//        val reference = getRepeatAddRef() { status ->
//            if (status.status != FuncStatus.SUCCESS) {
//                callback(status)
//            }
//        }
//        if (reference == null) {
//            val statusInfo = FuncStatusInfo(
//                status = FuncStatus.FAILED,
//                errorMessage = "reference is null"
//            )
//            return statusInfo
//        }
//
//        val ret = addDataToRTDbWithPush(repeatAdd, reference, callback = callback)
//        return ret
//    }
//
//    suspend fun updateRepeatAdd(
//        repeatAdd: RepeatAdd,
//        callback: (FuncStatusInfo) -> Unit = {}
//    ): FuncStatusInfo {
//        val funcName = ::updateRepeatAdd.name
//        LogClassFuncCalled(className, funcName)
//        val reference = getRepeatAddRef { status ->
//            if (status.status != FuncStatus.SUCCESS) {
//                callback(status)
//            }
//        }
//        if (reference == null) {
//            val statusInfo = FuncStatusInfo(
//                status = FuncStatus.FAILED,
//                errorMessage = "reference is null"
//            )
//            return statusInfo
//        }
//        val ret = updateDataToRTDb(repeatAdd, reference, callback = callback)
//        return ret
//    }
//
//    suspend fun removeRepeatAdd(
//        repeatAdd: RepeatAdd,
//        callback: (FuncStatusInfo) -> Unit = {}
//    ): FuncStatusInfo {
//        val funcName = ::removeRepeatAdd.name
//        LogClassFuncCalled(className, funcName)
//        val reference = getRepeatAddRef() { status ->
//            if (status.status != FuncStatus.SUCCESS) {
//                callback(status)
//            }
//        }
//        if (reference == null) {
//            return FuncStatusInfo(
//                status = FuncStatus.FAILED,
//                errorMessage = "reference is null"
//            )
//        }
//        val ret = removeDataFromRTDb(repeatAdd, reference, callback = callback)
//
//        return ret
//    }
//}