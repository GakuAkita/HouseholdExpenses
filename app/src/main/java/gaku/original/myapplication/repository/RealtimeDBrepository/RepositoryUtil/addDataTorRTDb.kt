import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CommonProperty
import gaku.original.myapplication.data.Interface.HasId
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.utility.LogAkitaDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

/**
 * ただReferenceに追加するだけ
 */
suspend fun <T> addDataToRTDbSimple(
    data: T,
    reference: DatabaseReference,
    timeout: Long = 2000,
): SuspendFuncStatusInfo = withContext(Dispatchers.IO) {
    val funcName = "addDataToRTDbSimple"
    LogAkitaDebug("${funcName} called")
    try {
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                LogAkitaDebug("${data}")
                reference.setValue(data)
                    .addOnCompleteListener { task ->
                        val statusInfo = if (task.isSuccessful) {
                            Log.d(funcName, "Data added successfully")
                            SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, "")
                        } else {
                            Log.e(funcName, "Failed to add data", task.exception)
                            SuspendFuncStatusInfo(
                                SuspendFuncStatus.FAILED,
                                task.exception?.message ?: "Unknown error"
                            )
                        }
                        continuation.resume(statusInfo)
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        val statusInfo = SuspendFuncStatusInfo(
            SuspendFuncStatus.TIMEOUT,
            "Timeout occurred"
        )
        statusInfo
    } catch (e: Exception) {
        val statusInfo = SuspendFuncStatusInfo(
            SuspendFuncStatus.FAILED,
            e.message ?: "Unknown error"
        )
        statusInfo
    }
}


/**
 * 親referenceを渡して、その下にpushして追加する
 */
suspend fun <T : HasId> addDataToRTDbWithId(
    data: T,
    reference: DatabaseReference,
    timeout: Long = 2000,
): SuspendFuncStatusInfo {
    val newDataRef = reference.push()
    data.id = newDataRef.key
    return addDataToRTDbSimple(
        data = data,
        reference = newDataRef,
        timeout = timeout
    )
}

suspend fun <T : CommonProperty> addDataToRTDbWithCommonProperty(
    data: T,
    reference: DatabaseReference,
    timeout: Long = 2000,
): SuspendFuncStatusInfo {

    val newDataRef = reference.push()

    data.id = newDataRef.key
    data.timestamp = System.currentTimeMillis()

    return addDataToRTDbSimple(
        data = data,
        reference = newDataRef,
        timeout = timeout
    )
}