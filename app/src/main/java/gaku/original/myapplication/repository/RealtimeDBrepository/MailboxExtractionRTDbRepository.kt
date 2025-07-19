package gaku.original.myapplication.repository.RealtimeDBrepository

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.RealtimeDbReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.EmailTemplateType
import gaku.original.myapplication.data.mapFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import updateAnyDataToRTDb
import javax.inject.Inject

class MailboxExtractionRTDbRepository @Inject constructor(
    private val realtimeDbReference: RealtimeDbReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    suspend fun getEmailTemplateSettingSingleRef(
        type: EmailTemplateType
    ): FetchResult<DatabaseReference> {
        return realtimeDbReference.getMailboxExtractionEmailTemplateSettingSingleRef(type)
    }

    suspend fun updateMailTypeSetting(
        setting: EmailTemplateType
    ): SuspendFuncStatusInfo {
        val refRet = getEmailTemplateSettingSingleRef(setting)
        if (refRet !is FetchResult.Success) {
            return refRet.toSuspendFuncStatusInfo()
        }

        val ref = refRet.data
        val ret = updateAnyDataToRTDb(setting, reference = ref)
        return ret
    }

    suspend fun getMailTypeSetting(
        setting: EmailTemplateType
    ): FetchResult<EmailTemplateType> {
        val funcName = ::getMailTypeSetting.name
        val refRet = getEmailTemplateSettingSingleRef(setting)
        if (refRet !is FetchResult.Success) {
            return refRet.mapFailure()
        }

        val ref = refRet.data

        return try {
            withTimeout(10000) {
                withContext(Dispatchers.IO) {
                    val snapshot = ref.get().await()
                    if (!snapshot.exists()) {
                        val result = FetchResult.Success(
                            data = setting.defaultInstance(),
                            isEmpty = true
                        )
                        return@withContext result
                    }
                    val data = snapshot.getValue(setting::class.java)
                    if (data == null) {
                        val result = FetchResult.Failure.GenericFailure(
                            status = SuspendFuncStatus.FAILED,
                            errorMessage = "Unable to convert data to ${setting::class.simpleName}"
                        )
                        return@withContext result
                    } else {
                        val result = FetchResult.Success(
                            data = data
                        )
                        return@withContext result
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "${funcName} Timeout.")
            FetchResult.Failure.Timeout()
        } catch (e: Exception) {
            FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }
}