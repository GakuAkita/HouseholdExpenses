import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.HasId
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.utility.toMap
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

suspend fun <T : HasId> updateDataToRTDb(
    data: T,
    reference: DatabaseReference, // データ参照を取得するための関数
    timeout: Long = 2000,
): SuspendFuncStatusInfo {
    val funcName = "updateDataToRTDb"

    val id = data.id
    if (id.isNullOrEmpty()) {
        Log.e(funcName, "id is null or empty")
        val statusInfo = SuspendFuncStatusInfo(
            status = SuspendFuncStatus.FAILED,
            errorMessage = "id is null or empty"
        )
        return statusInfo
    }

    val updateRef = reference.child(id)
    return try {
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                updateRef.setValue(data)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(funcName, "Data updated successfully")
                            val statusInfo = SuspendFuncStatusInfo(
                                status = SuspendFuncStatus.SUCCESS,
                                errorMessage = ""
                            )
                            continuation.resume(statusInfo)
                        } else {
                            Log.e(funcName, "Failed to update data", task.exception)
                            val statusInfo = SuspendFuncStatusInfo(
                                status = SuspendFuncStatus.FAILED,
                                errorMessage = task.exception?.message ?: "Unknown error"
                            )
                            continuation.resume(statusInfo)
                        }
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        Log.e(funcName, "Timeout occurred")
        val statusInfo = SuspendFuncStatusInfo(
            status = SuspendFuncStatus.TIMEOUT,
            errorMessage = "Timeout occurred"
        )
        return statusInfo
    } catch (e: Exception) {
        Log.e(funcName, "Exception occurred", e)
        val statusInfo = SuspendFuncStatusInfo(
            status = SuspendFuncStatus.FAILED,
            errorMessage = e.message ?: "Unknown error"
        )
        return statusInfo
    }
}

suspend fun <T : Any> updateAnyDataToRTDb(
    data: T,
    reference: DatabaseReference,
    timeout: Long = 2000,
): SuspendFuncStatusInfo {
    val funcName = "updateAnyDataToRTDb"
    return try {
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                reference.updateChildren(data.toMap(data::class.java as Class<T>))//ちょっとよくわからないけどこれいく？GPTに作ってもらった。
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(funcName, "Data updated successfully")
                            val statusInfo = SuspendFuncStatusInfo(
                                status = SuspendFuncStatus.SUCCESS,
                                errorMessage = ""
                            )
                            continuation.resume(statusInfo)
                        } else {
                            Log.e(funcName, "Failed to update data", task.exception)
                            val statusInfo = SuspendFuncStatusInfo(
                                status = SuspendFuncStatus.FAILED,
                                errorMessage = task.exception?.message ?: "Unknown error"
                            )
                            continuation.resume(statusInfo)
                        }
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        Log.e(funcName, "Timeout occurred")
        val statusInfo = SuspendFuncStatusInfo(
            status = SuspendFuncStatus.TIMEOUT,
            errorMessage = "Timeout occurred"
        )
        return statusInfo
    } catch (e: Exception) {
        Log.e(funcName, "Exception occurred", e)
        val statusInfo = SuspendFuncStatusInfo(
            status = SuspendFuncStatus.FAILED,
            errorMessage = e.message ?: "Unknown error"
        )
        return statusInfo
    }
}
