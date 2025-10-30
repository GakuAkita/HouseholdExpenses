import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.Interface.CommonProperty
import gaku.original.myapplication.data.Interface.HasId
import gaku.original.myapplication.utility.LogAkitaDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
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
): FuncStatusInfo = withContext(Dispatchers.IO) {
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
                            FuncStatusInfo(FuncStatus.SUCCESS, "")
                        } else {
                            Log.e(funcName, "Failed to add data", task.exception)
                            FuncStatusInfo(
                                FuncStatus.FAILED,
                                task.exception?.message ?: "Unknown error"
                            )
                        }
                        continuation.resume(statusInfo)
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        Log.w(funcName, "Timeout occurred — checking local cache reflection")

        val isReflected = try {
            delay(100) // 少し待ってローカル反映を確実にする
            isLocalWriteReflected(reference, data)
        } catch (inner: Exception) {
            Log.w(funcName, "Local reflection check failed: ${inner.message}")
            false
        }

        if (isReflected) {
            Log.i(funcName, "Local cache reflects data; treating as SUCCESS")
            FuncStatusInfo(FuncStatus.SUCCESS, "Reflected locally (offline mode)")
        } else {
            Log.w(funcName, "Local cache not updated; timeout remains.")
            FuncStatusInfo(FuncStatus.TIMEOUT, "Timeout (not reflected locally)")
        }
    } catch (e: Exception) {
        val statusInfo = FuncStatusInfo(
            FuncStatus.FAILED,
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
): FuncStatusInfo {
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
): FuncStatusInfo {

    val newDataRef = reference.push()

    data.id = newDataRef.key
    data.timestamp = System.currentTimeMillis()

    return addDataToRTDbSimple(
        data = data,
        reference = newDataRef,
        timeout = timeout
    )
}