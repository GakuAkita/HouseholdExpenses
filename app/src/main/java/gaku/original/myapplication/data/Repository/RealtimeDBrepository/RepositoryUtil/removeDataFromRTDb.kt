import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CommonProperty
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

suspend fun <T : CommonProperty> removeDataFromRTDb(
    data: T,
    reference: DatabaseReference,
    timeout: Long = 2000,
    callback: (SuspendFuncStatus) -> Unit // callback を追加
): SuspendFuncStatus {
    val funcName = "removeDataFromRTDb"

    val id = data.id
    if (id.isNullOrEmpty()) {
        Log.e(funcName, "id is null or empty")
        callback(SuspendFuncStatus.FAILED) // idがnullや空なら、失敗をcallbackで通知
        return SuspendFuncStatus.FAILED // 即終了
    }

    return try {
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                val removeRef = reference.child(id)

                removeRef.removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(funcName, "Data removed successfully")
                            callback(SuspendFuncStatus.SUCCESS) // 成功した場合にcallbackを呼び出す
                            continuation.resume(SuspendFuncStatus.SUCCESS)
                        } else {
                            Log.e(funcName, "Failed to remove data", task.exception)
                            callback(SuspendFuncStatus.FAILED) // 失敗した場合にcallbackを呼び出す
                            continuation.resume(SuspendFuncStatus.FAILED)
                        }
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        Log.e(funcName, "Timeout occurred")
        callback(SuspendFuncStatus.TIMEOUT) // タイムアウトの場合にcallbackを呼び出す
        return SuspendFuncStatus.TIMEOUT
    } catch (e: Exception) {
        Log.e(funcName, "Exception occurred", e)
        callback(SuspendFuncStatus.FAILED) // 例外が発生した場合にcallbackを呼び出す
        return SuspendFuncStatus.FAILED
    }
}
