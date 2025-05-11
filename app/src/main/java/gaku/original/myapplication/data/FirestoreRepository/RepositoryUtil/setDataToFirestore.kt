package gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

suspend fun setDataToFirestore(
    data: Any,
    reference: DocumentReference,
    timeout: Long = 2000,
    callback: (SuspendFuncStatusInfo) -> Unit = {},
): SuspendFuncStatus {
    val funcName = "setDataToFirestore"

    return try {
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                reference.set(data)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(funcName, "Data (${data}) was set successfully")
                            callback(SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, ""))
                            continuation.resume(
                                SuspendFuncStatus.SUCCESS
                            ) //  成功したので再開。これでSUCCESSが返るらしい
                        } else {
                            Log.e(funcName, "Failed to set data${data}", task.exception)
                            callback(
                                SuspendFuncStatusInfo(
                                    SuspendFuncStatus.FAILED,
                                    task.exception?.message ?: "不明なエラーが発生しました"
                                )
                            )
                            continuation.resume(SuspendFuncStatus.FAILED) //  失敗したので再開
                        }
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        callback(SuspendFuncStatusInfo(SuspendFuncStatus.TIMEOUT, "タイムアウトが発生しました。"))
        return SuspendFuncStatus.TIMEOUT
    } catch (e: Exception) {
        callback(SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, "${e.message}"))
        return SuspendFuncStatus.FAILED
    }
}