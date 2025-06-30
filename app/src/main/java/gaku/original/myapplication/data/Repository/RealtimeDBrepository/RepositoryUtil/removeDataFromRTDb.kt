import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CommonProperty
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

suspend fun <T : CommonProperty> removeDataFromRTDb(
    data: T,
    reference: DatabaseReference,
    timeout: Long = 2000,
    callback: (SuspendFuncStatusInfo) -> Unit // callback を追加
): SuspendFuncStatusInfo {
    val funcName = "removeDataFromRTDb"

    val id = data.id
    if (id.isNullOrEmpty()) {
        Log.e(funcName, "id is null or empty")
        val statusInfo = SuspendFuncStatusInfo(
            status = SuspendFuncStatus.FAILED,
            errorMessage = "id is null or empty"
        )
        callback(statusInfo) // idがnullや空なら、失敗をcallbackで通知
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
                            callback(statusInfo) // 成功した場合にcallbackを呼び出す
                            continuation.resume(statusInfo)
                        } else {
                            Log.e(funcName, "Failed to remove data", task.exception)
                            val statusInfo = SuspendFuncStatusInfo(
                                status = SuspendFuncStatus.FAILED,
                                errorMessage = task.exception?.message ?: "Unknown error"
                            )
                            callback(statusInfo) // 失敗した場合にcallbackを呼び出す
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
        callback(statusInfo) // タイムアウトの場合にcallbackを呼び出す
        return statusInfo
    } catch (e: Exception) {
        Log.e(funcName, "Exception occurred", e)
        val statusInfo = SuspendFuncStatusInfo(
            status = SuspendFuncStatus.FAILED,
            errorMessage = e.message ?: "Unknown error"
        )
        callback(statusInfo) // 例外が発生した場合にcallbackを呼び出す
        return statusInfo
    }
}
