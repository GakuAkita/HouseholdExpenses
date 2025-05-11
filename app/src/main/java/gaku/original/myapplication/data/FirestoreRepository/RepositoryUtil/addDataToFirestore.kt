import android.util.Log
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CommonProperty
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

suspend fun <T : CommonProperty> addDataToFirestore(
    data: T,
    reference: CollectionReference,
    timeout: Long = 3000,
    callback: (SuspendFuncStatusInfo) -> Unit = {}
): SuspendFuncStatusInfo {
    val funcName = "addDataToFirestore"

    data.timestamp = System.currentTimeMillis()

    return try {
        withTimeout(timeout) {
            //Firestoreにデータを一回追加(ここでIDが生成される)
            val newDataRef = reference.add(data).await()

            //生成されたIDをインスタンスに設定

            suspendCancellableCoroutine { continuation ->
                reference.add(data)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(funcName, "Data added successfully")
                            val documentReference = task.result
                            val documentId = docu
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