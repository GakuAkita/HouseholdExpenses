package gaku.original.myapplication.data.RepositoryUtil

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

suspend fun addSingleDataToRTDb(
    data: Any, /* 文字列、数値、booleanのどれか */
    keyName: String,/* キーの名前(childの名前) */
    reference: DatabaseReference,
    callback: (SuspendFuncStatus) -> Unit = {}
): SuspendFuncStatus {
    val funcName = "addSingleDataToRTDb"

    //data型の確認
    when (data) {
        is String, is Boolean, is Number -> {
            /* 特に問題ないので、次に行く */
        }

        else -> {
            Log.d(funcName, "passed data's type is wrong.${data.javaClass.simpleName}")
            callback(SuspendFuncStatus.FAILED)
            return SuspendFuncStatus.FAILED
        }
    }

    return try {
        withTimeout(2000) {
            suspendCancellableCoroutine { continuation ->
                reference.child(keyName).setValue(data)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(funcName, "Data added successfully")
                            callback(SuspendFuncStatus.SUCCESS)
                            continuation.resume(SuspendFuncStatus.SUCCESS) //  成功したので再開。これでSUCCESSが返るらしい
                        } else {
                            Log.e(funcName, "Failed to add data", task.exception)
                            callback(SuspendFuncStatus.FAILED)
                            continuation.resume(SuspendFuncStatus.FAILED) //  失敗したので再開
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
