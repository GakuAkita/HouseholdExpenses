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
    callback: (SuspendFuncStatusInfo, DocumentReference?) -> Unit = { _, _ -> }
): Pair<SuspendFuncStatusInfo, DocumentReference?> {
    return try {
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                reference.add(data)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(
                                Pair(
                                    SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, ""),
                                    task.result
                                )
                            )
                        } else {
                            continuation.resume(
                                Pair(
                                    SuspendFuncStatusInfo(
                                        SuspendFuncStatus.FAILED,
                                        task.exception?.message ?: "不明なエラーが発生しました"
                                    ),
                                    null
                                )
                            )
                        }
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        Pair(
            SuspendFuncStatusInfo(SuspendFuncStatus.TIMEOUT, "タイムアウトが発生しました。"),
            null
        )
    } catch (e: Exception) {
        Pair(
            SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, e.message ?: "不明なエラー"),
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
): SuspendFuncStatus {
    val funcName = "setDataToFirestore"

    return try {
        withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                reference.set(data)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(funcName, "Data ($data) was set successfully")
                            callback(SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, ""))
                            continuation.resume(SuspendFuncStatus.SUCCESS)
                        } else {
                            Log.e(funcName, "Failed to set data $data", task.exception)
                            callback(
                                SuspendFuncStatusInfo(
                                    SuspendFuncStatus.FAILED,
                                    task.exception?.message ?: "不明なエラーが発生しました"
                                )
                            )
                            continuation.resume(SuspendFuncStatus.FAILED)
                        }
                    }
            }
        }
    } catch (e: TimeoutCancellationException) {
        callback(SuspendFuncStatusInfo(SuspendFuncStatus.TIMEOUT, "タイムアウトしました。"))
        return SuspendFuncStatus.TIMEOUT
    } catch (e: Exception) {
        callback(SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, "${e.message}"))
        return SuspendFuncStatus.FAILED
    }
}
