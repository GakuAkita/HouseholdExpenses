package gaku.original.myapplication.repository.RealtimeDBrepository

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.RealtimeDbReference
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.EmailTemplateType
import gaku.original.myapplication.data.dataClass.MailboxExtractionLastExec
import gaku.original.myapplication.data.mapFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import setAnyDataToRTDb
import updateAnyDataToRTDb
import javax.inject.Inject

class MailboxExtractionRTDbRepository @Inject constructor(
    private val realtimeDbReference: RealtimeDbReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    suspend fun getEmailTemplateSettingSingleRef(
        type: EmailTemplateType
    ): FuncResultWithData<DatabaseReference> {
        return realtimeDbReference.getMailboxExtractionEmailTemplateSettingSingleRef(type)
    }

    suspend fun updateMailTypeSetting(
        setting: EmailTemplateType,
        force: Boolean = false
    ): FuncStatusInfo {
        val refRet = getEmailTemplateSettingSingleRef(setting)
        if (refRet !is FuncResultWithData.Success) {
            return refRet.toFuncStatusInfo()
        }

        val ref = refRet.data
        val ret = if (force) setAnyDataToRTDb(setting, reference = ref) else updateAnyDataToRTDb(
            setting,
            reference = ref
        )
        return ret
    }

    suspend fun getMailTypeSetting(
        setting: EmailTemplateType
    ): FuncResultWithData<EmailTemplateType> {
        val funcName = ::getMailTypeSetting.name
        val refRet = getEmailTemplateSettingSingleRef(setting)
        if (refRet !is FuncResultWithData.Success) {
            return refRet.mapFailure()
        }

        val ref = refRet.data

        return try {
            withTimeout(10000) {
                withContext(Dispatchers.IO) {
                    val snapshot = ref.get().await()
                    if (!snapshot.exists()) {
                        val result = FuncResultWithData.Success(
                            data = setting.defaultInstance(),
                            isEmpty = true
                        )
                        return@withContext result
                    }
                    val data = snapshot.getValue(setting::class.java)
                    if (data == null) {
                        val result = FuncResultWithData.Failure.GenericFailure(
                            status = FuncStatus.FAILED,
                            errorMessage = "Unable to convert data to ${setting::class.simpleName}"
                        )
                        return@withContext result
                    } else {
                        val result = FuncResultWithData.Success(
                            data = data
                        )
                        return@withContext result
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "${funcName} Timeout.")
            FuncResultWithData.Failure.Timeout()
        } catch (e: Exception) {
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun getIsGmailTokenExist(
        email: String? = null /* nullだったらcurrentUserEmailが使われる */
    ): FuncResultWithData<Boolean> {
        val funcName = ::getIsGmailTokenExist.name

        val refRet =
            if (email != null) realtimeDbReference.getMailboxExtractionGmailTokenSingleRef(email)
            else realtimeDbReference.getMailboxExtractionGmailTokenSingleRef( /* デフォルト値 */)

        if (refRet !is FuncResultWithData.Success) {
            return refRet.mapFailure()
        }
        val ref = refRet.data

        return try {
            withTimeout(10000) {
                withContext(Dispatchers.IO) {
                    val snapshot = ref.get().await()
                    if (snapshot.exists()) {
                        return@withContext FuncResultWithData.Success(data = true)
                    } else {
                        return@withContext FuncResultWithData.Success(data = false)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "${funcName} Timeout.")
            FuncResultWithData.Failure.Timeout()
        } catch (e: Exception) {
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun getMailTypeLastExec(
        type: EmailTemplateType
    ): FuncResultWithData<MailboxExtractionLastExec> {
        val funcName = ::getMailTypeLastExec.name
        val refRet = realtimeDbReference.getMailboxExtractionLastExecRef(type)
        if (refRet !is FuncResultWithData.Success) {
            return refRet.mapFailure()
        }
        val ref = refRet.data

        return try {
            withTimeout(10000) {
                withContext(Dispatchers.IO) {
                    val snapshot = ref.get().await()
                    if (!snapshot.exists()) {
                        return@withContext FuncResultWithData.Success(
                            data = MailboxExtractionLastExec(type.nodeName, 0L),
                            isEmpty = true
                        )
                    }
                    val data = snapshot.getValue(MailboxExtractionLastExec::class.java)
                    if (data == null) {
                        FuncResultWithData.Failure.GenericFailure(
                            status = FuncStatus.FAILED,
                            errorMessage = "Unable to convert data to MailboxExtractionLastExec"
                        )
                    } else {
                        FuncResultWithData.Success(data = data)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(className, "${funcName} Timeout.")
            FuncResultWithData.Failure.Timeout()
        } catch (e: Exception) {
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }
}