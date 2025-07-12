package gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.SetOptions
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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
            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { continuation ->
                    reference.add(data)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val statusInfo =
                                    SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, "")
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
 * これほぼupdateDataだな。
 * */
suspend fun setDataToFirestoreWithOption(
    data: Any,
    timeout: Long = 3000,
    reference: DocumentReference,
    setOptions: SetOptions? = null,
): SuspendFuncStatusInfo {
    val funcName = "setDataToFirestoreWithOption"

    return try {
        withTimeout(timeout) {
            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { continuation ->
                    val task = if (setOptions != null) {
                        reference.set(data, setOptions)
                    } else {
                        reference.set(data)
                    }

                    task.addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            Log.d(funcName, "Data ($data) was set successfully")
                            val statusInfo = SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, "")
                            continuation.resume(statusInfo)
                        } else {
                            Log.e(funcName, "Failed to set data $data", result.exception)
                            val statusInfo = SuspendFuncStatusInfo(
                                SuspendFuncStatus.FAILED,
                                result.exception?.message ?: "不明なエラーが発生しました"
                            )
                            continuation.resume(statusInfo)
                        }
                    }
                }
            }
        }
    } catch (e: TimeoutCancellationException) {
        val statusInfo = SuspendFuncStatusInfo(SuspendFuncStatus.TIMEOUT, "タイムアウトしました。")
        return statusInfo
    } catch (e: Exception) {
        val statusInfo = SuspendFuncStatusInfo(SuspendFuncStatus.FAILED, "${e.message}")
        return statusInfo
    }
}

suspend fun setDataToFirestore(
    data: Any,
    timeout: Long = 3000,
    reference: DocumentReference,
): SuspendFuncStatusInfo {
    return setDataToFirestoreWithOption(
        data = data,
        timeout = timeout,
        reference = reference,
        setOptions = null, // merge しない通常の set
    )
}

/* setなんだけど、存在しなければ作る */
suspend fun mergeDataToFirestore(
    data: Any,
    timeout: Long = 3000,
    reference: DocumentReference,
): SuspendFuncStatusInfo {
    return setDataToFirestoreWithOption(
        data = data,
        timeout = timeout,
        reference = reference,
        setOptions = SetOptions.merge(), // merge する
    )
}


suspend fun removeDocument(
    reference: DocumentReference,
    timeout: Long = 3000,  // デフォルトのタイムアウト時間（ミリ秒）
): SuspendFuncStatusInfo {
    return try {
        // タイムアウトを設定して削除処理
        withTimeout(timeout) {
            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { continuation ->
                    reference.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val statusInfo =
                                    SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, "")
                                continuation.resume(statusInfo)
                            } else {
                                val statusInfo = SuspendFuncStatusInfo(
                                    SuspendFuncStatus.FAILED,
                                    task.exception?.message ?: "Failed to delete document"
                                )
                                continuation.resume(statusInfo)
                            }
                        }
                }
            }
        }
    } catch (e: TimeoutCancellationException) {
        val statusInfo =
            SuspendFuncStatusInfo(SuspendFuncStatus.TIMEOUT, "タイムアウトが発生しました。")
        return statusInfo
    } catch (e: Exception) {
        val statusInfo =
            SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                e.message ?: "不明なエラーが発生しました"
            )
        return statusInfo
    }
}
