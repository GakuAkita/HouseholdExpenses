package gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 引数のデータは一切いじらず、
 * ただFirestoreにアップロードする
 */
suspend fun addDataToFirestore(
    data: Any,
    reference: CollectionReference,
    timeout: Long = 3000,
): FuncResultWithData<DocumentReference?> {
    return try {
        withTimeout(timeout) {
            withContext(Dispatchers.IO) {
                val task = reference.add(data).await()
                val statusInfo = FuncStatusInfo(FuncStatus.SUCCESS, "")
                FuncResultWithData.Success(task)
            }
        }
    } catch (e: TimeoutCancellationException) {
        // タイムアウト発生時に pending 書き込みがあれば成功扱い
        val hasPending = try {
            val deferred = CompletableDeferred<Boolean>()
            var listener: ListenerRegistration? = null

            listener = reference.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    deferred.complete(false)
                    listener?.remove()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val pending = snapshot.metadata.hasPendingWrites()
                    deferred.complete(pending)
                    listener?.remove()
                }
            }

            withTimeoutOrNull(500) { deferred.await() } ?: false
        } catch (inner: Exception) {
            Log.w("addDataToFirestore", "Error checking pending writes: ${inner.message}")
            false
        }

        if (hasPending) {
            FuncResultWithData.Success(null)
        } else {
            FuncResultWithData.Failure.Timeout("タイムアウトが発生しました。")
        }
    } catch (e: Exception) {
        val statusInfo =
            FuncStatusInfo(FuncStatus.FAILED, e.message ?: "不明なエラー")
        FuncResultWithData.Failure.GenericFailure(
            status = FuncStatus.FAILED,
            errorMessage = e.message ?: "不明なエラー"
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
): FuncStatusInfo {
    val funcName = "setDataToFirestoreWithOption"

    return try {
        withTimeout(timeout) {
            withContext(Dispatchers.IO) {
                if (setOptions != null) {
                    reference.set(data, setOptions).await()
                } else {
                    reference.set(data).await()
                }
                Log.d(funcName, "Data ($data) was set successfully")
                FuncStatusInfo(FuncStatus.SUCCESS, "")
            }
        }
    } catch (e: TimeoutCancellationException) {
        // タイムアウト発生時に pending 書き込みがあれば成功扱い
        val hasPending = try {
            val deferred = CompletableDeferred<Boolean>()
            var listener: ListenerRegistration? = null

            listener = reference.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    deferred.complete(false)
                    listener?.remove()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val pending = snapshot.metadata.hasPendingWrites()
                    deferred.complete(pending)
                    listener?.remove()
                }
            }

            withTimeoutOrNull(500) { deferred.await() } ?: false
        } catch (inner: Exception) {
            Log.w(funcName, "Error checking pending writes: ${inner.message}")
            false
        }

        if (hasPending) {
            Log.w(funcName, "Timeout occurred but local cache updated — treating as SUCCESS")
            FuncStatusInfo(FuncStatus.SUCCESS, "ローカルキャッシュに反映されました。")
        } else {
            FuncStatusInfo(FuncStatus.TIMEOUT, "タイムアウトが発生しました。")
        }
    } catch (e: Exception) {
        Log.e(funcName, "Failed to set data $data", e)
        FuncStatusInfo(FuncStatus.FAILED, e.message ?: "不明なエラーが発生しました")
    }
}

suspend fun setDataToFirestore(
    data: Any,
    timeout: Long = 3000,
    reference: DocumentReference,
): FuncStatusInfo {
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
): FuncStatusInfo {
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
): FuncStatusInfo {
    val funcName = "removeDocument"
    return try {
        // タイムアウトを設定して削除処理
        withTimeout(timeout) {
            withContext(Dispatchers.IO) {
                reference.delete().await()
                FuncStatusInfo(FuncStatus.SUCCESS, "")
            }
        }
    } catch (e: TimeoutCancellationException) {
        // タイムアウト発生時に pending 書き込みがあれば成功扱い（または既にローカルで削除反映済み）
        val treatSuccess = try {
            val deferred = CompletableDeferred<Boolean>()
            var listener: ListenerRegistration? = null

            listener = reference.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    deferred.complete(false)
                    listener?.remove()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // 既に存在しない（ローカルで削除反映済み）なら成功
                    if (!snapshot.exists()) {
                        deferred.complete(true)
                        listener?.remove()
                        return@addSnapshotListener
                    }
                    // もしくは pending 書き込み中なら成功とみなす
                    val pending = snapshot.metadata.hasPendingWrites()
                    deferred.complete(pending)
                    listener?.remove()
                }
            }

            withTimeoutOrNull(500) { deferred.await() } ?: false
        } catch (inner: Exception) {
            Log.w(funcName, "Error checking pending writes: ${inner.message}")
            false
        }

        if (treatSuccess) {
            Log.w(
                funcName,
                "Timeout occurred but local cache updated — treating as SUCCESS"
            )
            FuncStatusInfo(FuncStatus.SUCCESS, "ローカルキャッシュに反映されました。")
        } else {
            val statusInfo =
                FuncStatusInfo(FuncStatus.TIMEOUT, "タイムアウトが発生しました。")
            return statusInfo
        }
    } catch (e: Exception) {
        val statusInfo =
            FuncStatusInfo(
                FuncStatus.FAILED,
                e.message ?: "不明なエラーが発生しました"
            )
        return statusInfo
    }
}
