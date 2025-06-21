import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CommonProperty
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

suspend fun <T : CommonProperty> updateDataToRTDb(
    data: T,
    reference: DatabaseReference, // データ参照を取得するための関数
    timeout: Long = 2000,
    callback: (SuspendFuncStatus) -> Unit // callback を追加
): SuspendFuncStatus {
    val funcName = "updateDataToRTDb"

    val id = data.id
    if (id.isNullOrEmpty()) {
        Log.e(funcName, "id is null or empty")
        callback(SuspendFuncStatus.FAILED) // callback を呼び出し
        return SuspendFuncStatus.FAILED
    }

    val updateRef = reference.child(id)
    return try {
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                updateRef.setValue(data)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(funcName, "Data updated successfully")
                            callback(SuspendFuncStatus.SUCCESS) // 成功時の callback 呼び出し
                            continuation.resume(SuspendFuncStatus.SUCCESS)
                        } else {
                            Log.e(funcName, "Failed to update data", task.exception)
                            callback(SuspendFuncStatus.FAILED) // 失敗時の callback 呼び出し
                            continuation.resume(SuspendFuncStatus.FAILED)
                        }
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        Log.e(funcName, "Timeout occurred")
        callback(SuspendFuncStatus.TIMEOUT) // タイムアウト時の callback 呼び出し
        return SuspendFuncStatus.TIMEOUT
    } catch (e: Exception) {
        Log.e(funcName, "Exception occurred", e)
        callback(SuspendFuncStatus.FAILED) // 例外時の callback 呼び出し
        return SuspendFuncStatus.FAILED
    }
}
