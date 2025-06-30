import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CommonProperty
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

suspend fun <T : CommonProperty> addDataToRTDb(
    data: T,
    reference: DatabaseReference,
    timeout: Long = 2000,
    callback: (SuspendFuncStatusInfo) -> Unit = {}
): SuspendFuncStatusInfo {
    val funcName = "addDataToRTDb"

    val newDataRef = reference.push()
    data.id = newDataRef.key
    data.timestamp = System.currentTimeMillis()

    return try {
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                newDataRef.setValue(data)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(funcName, "Data added successfully")
                            val statusInfo = SuspendFuncStatusInfo(
                                status = SuspendFuncStatus.SUCCESS,
                                errorMessage = ""
                            )
                            callback(statusInfo)
                            continuation.resume(statusInfo) // ✅ 成功したので再開
                        } else {
                            Log.e(funcName, "Failed to add data", task.exception)
                            val statusInfo = SuspendFuncStatusInfo(
                                status = SuspendFuncStatus.FAILED,
                                errorMessage = task.exception?.message ?: "Unknown error"
                            )
                            callback(statusInfo)
                            continuation.resume(statusInfo) // ❌ 失敗したので再開
                        }
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        val statusInfo = SuspendFuncStatusInfo(
            status = SuspendFuncStatus.TIMEOUT,
            errorMessage = "Timeout occurred"
        )
        return statusInfo
    } catch (e: Exception) {
        val statusInfo = SuspendFuncStatusInfo(
            status = SuspendFuncStatus.FAILED,
            errorMessage = e.message ?: "Unknown error"
        )
        callback(statusInfo)
        return statusInfo
    }
}
