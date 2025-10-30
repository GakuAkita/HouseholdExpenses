import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.Interface.HasId
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

/**
 * データがローカルキャッシュに反映されているか確認。
 * addListenerForSingleValueEvent はオフラインでもローカルデータを返す。
 */
suspend fun <T> isLocalWriteReflected(
    reference: DatabaseReference,
    expected: T
): Boolean = suspendCancellableCoroutine { continuation ->
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val value = snapshot.value
            val isMatch = when (expected) {
                is String, is Number, is Boolean -> expected == value
                is Map<*, *>, is List<*> -> expected == value
                else -> snapshot.exists() && value != null
            }

            continuation.resume(isMatch)
            reference.removeEventListener(this) // 明示的に解除
        }

        override fun onCancelled(error: DatabaseError) {
            continuation.resume(false)
            reference.removeEventListener(this) // 安全解除
        }
    }

    reference.addListenerForSingleValueEvent(listener)

    continuation.invokeOnCancellation {
        // コルーチンキャンセル時にもリスナー解除
        reference.removeEventListener(listener)
    }
}

suspend fun <T : HasId> removeDataFromRTDb(
    data: T,
    reference: DatabaseReference,
    timeout: Long = 2000,
): FuncStatusInfo {
    val funcName = "removeDataFromRTDb"

    val id = data.id
    if (id.isNullOrEmpty()) {
        Log.e(funcName, "id is null or empty")
        val statusInfo = FuncStatusInfo(
            status = FuncStatus.FAILED,
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
                            val statusInfo = FuncStatusInfo(
                                status = FuncStatus.SUCCESS,
                                errorMessage = "Data removed successfully"
                            )
                            continuation.resume(statusInfo)
                        } else {
                            Log.e(funcName, "Failed to remove data", task.exception)
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
        // ⏱ タイムアウト発生時
        Log.w(funcName, "Timeout occurred — checking local cache reflection")

        // ローカルキャッシュに反映されているか確認
        val isReflected = try {
            delay(100) // 若干の反映遅延対策（optional）
            isLocalWriteReflected(reference, data)
        } catch (inner: Exception) {
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
        Log.e(funcName, "Exception occurred", e)
        val statusInfo = FuncStatusInfo(
            status = FuncStatus.FAILED,
            errorMessage = e.message ?: "Unknown error"
        )
        statusInfo
    }
}
