import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.HasId
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

suspend fun <T : HasId> removeDataFromRTDb(
    data: T,
    reference: DatabaseReference,
    timeout: Long = 2000,
): SuspendFuncStatusInfo {
    val funcName = "removeDataFromRTDb"

    val id = data.id
    if (id.isNullOrEmpty()) {
        Log.e(funcName, "id is null or empty")
        val statusInfo = SuspendFuncStatusInfo(
            status = SuspendFuncStatus.FAILED,
            errorMessage = "id is null or empty"
        )
        return statusInfo // 即終了
    }

    return try {
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                val removeRef = reference.child(id)

                removeRef.removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(funcName, "Data removed successfully")
                            val statusInfo = SuspendFuncStatusInfo(
                                status = SuspendFuncStatus.SUCCESS,
                                errorMessage = "Data removed successfully"
                            )
                            continuation.resume(statusInfo)
                        } else {
                            Log.e(funcName, "Failed to remove data", task.exception)
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
