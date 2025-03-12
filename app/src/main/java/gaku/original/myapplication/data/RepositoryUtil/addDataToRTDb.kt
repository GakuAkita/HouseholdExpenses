import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CommonProperty
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

suspend fun <T : CommonProperty> addDataToRTDb(
    data: T,
    reference: DatabaseReference,
    callback: (SuspendFuncStatus) -> Unit = {}
): SuspendFuncStatus {
    val funcName = "addDataToRTDb"

    val newDataRef = reference.push()
    data.id = newDataRef.key
    data.timestamp = System.currentTimeMillis()

    return try {
        withTimeout(2000) {
            suspendCancellableCoroutine { continuation ->
                newDataRef.setValue(data)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(funcName, "Data added successfully")
                            callback(SuspendFuncStatus.SUCCESS)
                            continuation.resume(SuspendFuncStatus.SUCCESS) // ✅ 成功したので再開
                        } else {
                            Log.e(funcName, "Failed to add data", task.exception)
                            callback(SuspendFuncStatus.FAILED)
                            continuation.resume(SuspendFuncStatus.FAILED) // ❌ 失敗したので再開
                        }
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        callback(SuspendFuncStatus.TIMEOUT)
        return SuspendFuncStatus.TIMEOUT
    } catch (e: Exception) {
        callback(SuspendFuncStatus.FAILED)
        return SuspendFuncStatus.FAILED
    }
}
