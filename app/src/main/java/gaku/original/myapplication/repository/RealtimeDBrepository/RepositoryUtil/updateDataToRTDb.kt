import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.Interface.HasId
import gaku.original.myapplication.utility.toMap
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

suspend fun <T : HasId> updateDataToRTDb(
    data: T,
    reference: DatabaseReference, // データ参照を取得するための関数
    timeout: Long = 2000,
): FuncStatusInfo {
    val funcName = "updateDataToRTDb"

    val id = data.id
    if (id.isNullOrEmpty()) {
        Log.e(funcName, "id is null or empty")
        val statusInfo = FuncStatusInfo(
            status = FuncStatus.FAILED,
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
                            val statusInfo = FuncStatusInfo(
                                status = FuncStatus.SUCCESS,
                                errorMessage = ""
                            )
                            continuation.resume(statusInfo)
                        } else {
                            Log.e(funcName, "Failed to update data", task.exception)
                            val statusInfo = FuncStatusInfo(
                                status = FuncStatus.FAILED,
                                errorMessage = task.exception?.message ?: "Unknown error"
                            )
                            continuation.resume(statusInfo)
                        }
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        Log.e(funcName, "Timeout occurred")
        val statusInfo = FuncStatusInfo(
            status = FuncStatus.TIMEOUT,
            errorMessage = "Timeout occurred"
        )
        return statusInfo
    } catch (e: Exception) {
        Log.e(funcName, "Exception occurred", e)
        val statusInfo = FuncStatusInfo(
            status = FuncStatus.FAILED,
            errorMessage = e.message ?: "Unknown error"
        )
        return statusInfo
    }
}

suspend fun <T : Any> updateAnyDataToRTDb(
    data: T,
    reference: DatabaseReference,
    timeout: Long = 2000,
): FuncStatusInfo {
    val funcName = "updateAnyDataToRTDb"
    return try {
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                reference.updateChildren(data.toMap(data::class.java as Class<T>))//ちょっとよくわからないけどこれいく？GPTに作ってもらった。
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(funcName, "Data updated successfully")
                            val statusInfo = FuncStatusInfo(
                                status = FuncStatus.SUCCESS,
                                errorMessage = ""
                            )
                            continuation.resume(statusInfo)
                        } else {
                            Log.e(funcName, "Failed to update data", task.exception)
                            val statusInfo = FuncStatusInfo(
                                status = FuncStatus.FAILED,
                                errorMessage = task.exception?.message ?: "Unknown error"
                            )
                            continuation.resume(statusInfo)
                        }
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        Log.e(funcName, "Timeout occurred")
        val statusInfo = FuncStatusInfo(
            status = FuncStatus.TIMEOUT,
            errorMessage = "Timeout occurred"
        )
        return statusInfo
    } catch (e: Exception) {
        Log.e(funcName, "Exception occurred", e)
        val statusInfo = FuncStatusInfo(
            status = FuncStatus.FAILED,
            errorMessage = e.message ?: "Unknown error"
        )
        return statusInfo
    }
}

suspend fun <T : Any> setAnyDataToRTDb(
    data: T,
    reference: DatabaseReference,
    timeout: Long = 2000,
): FuncStatusInfo {
    val funcName = "setAnyDataToRTDb"
    return try {
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                reference.setValue(data)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(funcName, "Data set successfully")
                            continuation.resume(
                                FuncStatusInfo(
                                    status = FuncStatus.SUCCESS,
                                    errorMessage = ""
                                )
                            )
                        } else {
                            Log.e(funcName, "Failed to set data", task.exception)
                            continuation.resume(
                                FuncStatusInfo(
                                    status = FuncStatus.FAILED,
                                    errorMessage = task.exception?.message ?: "Unknown error"
                                )
                            )
                        }
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        Log.e(funcName, "Timeout occurred")
        return FuncStatusInfo(
            status = FuncStatus.TIMEOUT,
            errorMessage = "Timeout occurred"
        )
    } catch (e: Exception) {
        Log.e(funcName, "Exception occurred", e)
        return FuncStatusInfo(
            status = FuncStatus.FAILED,
            errorMessage = e.message ?: "Unknown error"
        )
    }
}

