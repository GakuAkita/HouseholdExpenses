package gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

/**
 * 引数のデータは一切いじらず、
 * ただFirestoreにアップロードする
 */
suspend fun addDataToFirestore(
    data: Any,
    reference: CollectionReference,
    timeout: Long = 3000,
    callback: (SuspendFuncStatusInfo) -> Unit = { _ -> }
): Pair<SuspendFuncStatusInfo, DocumentReference?> {
    return try {
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                reference.add(data)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val statusInfo = SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, "")
                            callback(statusInfo)
                            continuation.resume(
                                Pair(
                                    statusInfo,
                                    task.result
                                )
                            )
                        } else {
                            val statusInfo = SuspendFuncStatusInfo(
                                SuspendFuncStatus.FAILED,
                                task.exception?.message ?: "不明なエラーが発生しました"
                            )
                            callback(statusInfo)
                            continuation.resume(
                                Pair(
                                    statusInfo,
                                    null
                                )
                            )
                        }
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        val statusInfo =
            SuspendFuncStatusInfo(SuspendFuncStatus.TIMEOUT, "タイムアウトが発生しました。")
        callback(statusInfo)
        Pair(
            statusInfo,
            null
        )
    } catch (e: Exception) {
        val statusInfo =
            SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, e.message ?: "不明なエラー")
        callback(statusInfo)
        Pair(
            statusInfo,
            null
        )
    }
}


/**
 * reference.addなのか、reference.setだけが違うから、それ以外を共通化
 * */
suspend fun setDataToFirestore(
    data: Any,
    timeout: Long = 3000,
    reference: DocumentReference,
    callback: (SuspendFuncStatusInfo) -> Unit = {},
): SuspendFuncStatusInfo {
    val funcName = "setDataToFirestore"

    return try {
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                reference.set(data)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(funcName, "Data ($data) was set successfully")
                            val statusInfo = SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, "")
                            callback(statusInfo)
                            continuation.resume(statusInfo)
                        } else {
                            Log.e(funcName, "Failed to set data $data", task.exception)
                            val statusInfo = SuspendFuncStatusInfo(
                                SuspendFuncStatus.FAILED,
                                task.exception?.message ?: "不明なエラーが発生しました"
                            )
                            callback(statusInfo)
                            continuation.resume(statusInfo)
                        }
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        val statusInfo = SuspendFuncStatusInfo(SuspendFuncStatus.TIMEOUT, "タイムアウトしました。")
        callback(statusInfo)
        return statusInfo
    } catch (e: Exception) {
        val statusInfo = SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, "${e.message}")
        callback(statusInfo)
        return statusInfo
    }
}
