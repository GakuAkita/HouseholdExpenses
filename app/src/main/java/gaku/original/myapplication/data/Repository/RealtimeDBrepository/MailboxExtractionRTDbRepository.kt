package gaku.original.myapplication.data.Repository.RealtimeDBrepository

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.RealtimeDbReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.MailboxExtractionCommon
import gaku.original.myapplication.data.dataClass.getMailboxExtractionInternalClass
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import updateAnyDataToRTDb
import javax.inject.Inject

class MailboxExtractionRTDbRepository @Inject constructor(
    private val realtimeDbReference: RealtimeDbReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    suspend fun getMailTypeSettingSingleRef(
        type: MailboxExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): DatabaseReference? {
        return realtimeDbReference.getMailboxExtractionMailTypeSettingSingleRef(type, callback)
    }

    suspend fun updateMailTypeSetting(
        setting: MailboxExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val funcName = ::updateMailTypeSetting.name
        var ret = SuspendFuncStatusInfo(
            status = SuspendFuncStatus.SUCCESS,
            errorMessage = ""
        )
        val ref = getMailTypeSettingSingleRef(setting, callback = {
            if (it.status != SuspendFuncStatus.SUCCESS) {
                ret = it
            }
        })
        if (ret.status != SuspendFuncStatus.SUCCESS || ref == null) {
            /* refはnullならStatusもSuccessではないはず */
            callback(ret)
            return ret
        }

        ret = updateAnyDataToRTDb(setting, reference = ref, callback = callback)
        return ret
    }

    suspend fun getMailTypeSetting(
        setting: MailboxExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): FetchResult<MailboxExtractionCommon> {
        val funcName = ::getMailTypeSetting.name
        var ret = FetchResult<MailboxExtractionCommon>(
            status = SuspendFuncStatus.SUCCESS,
            errorMessage = ""
        )
        val ref = getMailTypeSettingSingleRef(setting, callback = {
            if (it.status != SuspendFuncStatus.SUCCESS) {
                ret = FetchResult(it.status, it.errorMessage)
            }
        })
        if (ret.status != SuspendFuncStatus.SUCCESS || ref == null) {
            /* refはnullならStatusもSuccessではないはず */
            callback(ret.toSuspendFuncStatusInfo())
            return ret
        }

        /* インスタンスからクラス名を取得 */
        val kClass = getMailboxExtractionInternalClass(setting)
        if (kClass == null) {
            val errResult = FetchResult<MailboxExtractionCommon>(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "Unknown type: ${setting::class.simpleName}"
            )
            callback(errResult.toSuspendFuncStatusInfo())
            return errResult
        }

        return try {
            withTimeout(10000) {
                val snapshot = ref.get().await()
                val data = snapshot.getValue(kClass.java)
                if (data == null) {
                    val result = FetchResult<MailboxExtractionCommon>(
                        status = SuspendFuncStatus.SUCCESS,//呼び出し側でnullなのかチェックを。
                        errorMessage = "まだデータが保存されていません"
                    )
                    callback(result.toSuspendFuncStatusInfo())
                    return@withTimeout result
                }
                val result = FetchResult(
                    status = SuspendFuncStatus.SUCCESS,
                    errorMessage = "Success",
                    data = data
                )
                callback(result.toSuspendFuncStatusInfo())
                result
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "${funcName} Timeout.")
            val result = FetchResult<MailboxExtractionCommon>(
                status = SuspendFuncStatus.TIMEOUT,
                errorMessage = "タイムアウトしました"
            )
            callback(result.toSuspendFuncStatusInfo())
            result
        } catch (e: Exception) {
            Log.d(className, "${funcName} failed. ${e.message}")
            val result = FetchResult<MailboxExtractionCommon>(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "${e.message}"
            )
            callback(result.toSuspendFuncStatusInfo())
            result
        }
    }
}