package gaku.original.myapplication

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.MailboxExtraction
import gaku.original.myapplication.data.dataClass.MailboxExtractionCommon
import gaku.original.myapplication.data.dataClass.getMailboxExtractionInternalClass
import gaku.original.myapplication.utility.LogException
import gaku.original.myapplication.utility.LogTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject


/* @TODO throwを出してしまうとアプリがクラッシュするらしい。したがって、nullのときの回避策を作る必要ある。*/
class RealtimeDbReference @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    val className: String = this::class.simpleName ?: "UnableToGetClassName"

    private val database = FirebaseDatabase
        .getInstance("https://householdexpenses2-default-rtdb.asia-southeast1.firebasedatabase.app")
        .reference//users配下にそれぞれのuserIdが存在

    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    //users配下の自分のuserIdのreferenceを返す
    // userId配下のexpenses
    suspend fun getUserRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): FetchResult<DatabaseReference> {
        val funcName = ::getUserRef.name
        return try {
            withTimeout(2000) {
                withContext(Dispatchers.IO) {
                    val userId: String = currentUserId ?: throw Exception("userId is null")

                    val ref: DatabaseReference = database.child("users").child(userId)
                    val result = FetchResult.Success(ref)
                    callback(result.toSuspendFuncStatusInfo())
                    result
                }
            }
        } catch (e: TimeoutCancellationException) {
            LogTimeout(className, funcName, e)
            val result = FetchResult.Failure.Timeout()
            callback(result.toSuspendFuncStatusInfo())
            result
        } catch (e: Exception) {
            LogException(className, funcName, e)
            val result = FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "${e.message}"
            )
            callback(result.toSuspendFuncStatusInfo())
            result
        }
    }

    /* getUserExpensesRefとgetUserCategoryRefで同じことをやっていたので共通化 */
    private suspend fun getUserChildrenRef(
        childrenPath: List<String>,/* たどり着きたい順に名前をいれていく */
        funcName: String,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): FetchResult<DatabaseReference> {
        val userRefRet = getUserRef()

        if (userRefRet !is FetchResult.Success) {
            callback(userRefRet.toSuspendFuncStatusInfo())
            return userRefRet
        }

        val userRef = userRefRet.data

        //シーケンスみたい。ある処理が終えたら次をスタートして、、みたいな。
        return try {
            val finalRef = withTimeout(2000) {
                withContext(Dispatchers.IO) {
                    var tmpRef = userRef
                    for (childName in childrenPath) {
                        tmpRef = tmpRef.child(childName)
                            ?: throw Exception("tmpRef became null at child: $childName")
                    }
                    tmpRef
                }
            }
            val result = FetchResult.Success(finalRef)
            callback(result.toSuspendFuncStatusInfo())
            result
        } catch (e: TimeoutCancellationException) {
            LogTimeout(className, funcName, e)
            val result = FetchResult.Failure.Timeout("Timeout: ${e.message}")
            callback(result.toSuspendFuncStatusInfo())
            result
        } catch (e: Exception) {
            LogException(className, funcName, e)
            val result = FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
            callback(result.toSuspendFuncStatusInfo())
            result
        }
    }

    // userId配下のexpenses
    suspend fun getUserExpenseRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): FetchResult<DatabaseReference> {
        val funcName = ::getUserExpenseRef.name
        Log.d(className, "${funcName} was called.")
        val childrenPath = listOf("data", "expenses")

        val ret = getUserChildrenRef(childrenPath, funcName, callback)
        return ret
    }

    //userId配下のcategory
    suspend fun getUserCategoryRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): FetchResult<DatabaseReference> {
        val funcName = ::getUserCategoryRef.name
        Log.d(className, "${funcName} was called")
        val childrenPath = listOf("data", "categories")

        val ret = getUserChildrenRef(childrenPath, funcName, callback)
        return ret
    }

    suspend fun getUserSettingsRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): FetchResult<DatabaseReference> {
        val funcName = ::getUserSettingsRef.name
        Log.d(className, "${funcName} was called")
        val childrenPath = listOf("settings")

        val ret = getUserChildrenRef(childrenPath, funcName, callback)
        return ret
    }

    suspend fun getUserRepeatAddRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): FetchResult<DatabaseReference> {
        val funcName = ::getUserRepeatAddRef.name
        Log.d(className, "${funcName} was called")
        val baseRefRet = getUserSettingsRef()
        if (baseRefRet !is FetchResult.Success) {//拡張関数を使うと、スマートキャストが効かない
            callback(baseRefRet.toSuspendFuncStatusInfo())
            return baseRefRet
        }

        val baseRef = baseRefRet.data
        val newRef = baseRef.child("repeatAdd")
        val result = FetchResult.Success(newRef)
        callback(result.toSuspendFuncStatusInfo())
        return result
    }

    /* MailboxExtraction配下 */
    private suspend fun getMailboxExtractionRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): FetchResult<DatabaseReference> {
        val funcName = ::getMailboxExtractionRef.name
        /* Log.d(className, "${funcName} was called") */
        val childrenPath = listOf("mailbox_extraction")

        val ret = getUserChildrenRef(childrenPath, funcName, callback)//callbackは中で実行される
        return ret
    }

    private suspend fun getMailboxExtractionMailTypeSettingsRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): FetchResult<DatabaseReference> {
        val baseRefRet = getMailboxExtractionRef()
        if (baseRefRet !is FetchResult.Success) {
            callback(baseRefRet.toSuspendFuncStatusInfo())
            return baseRefRet
        }

        val baseRef = baseRefRet.data
        val result = FetchResult.Success(
            baseRef.child("mail_type_settings")
        )
        callback(result.toSuspendFuncStatusInfo())
        return result
    }

    suspend fun getMailboxExtractionMailTypeSettingSingleRef(
        type: MailboxExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): FetchResult<DatabaseReference> {
        val baseRefRet = getMailboxExtractionMailTypeSettingsRef()
        if (baseRefRet !is FetchResult.Success) {
            callback(baseRefRet.toSuspendFuncStatusInfo())
            return baseRefRet
        }
        val baseRef = baseRefRet.data

        val result = FetchResult.Success(
            baseRef.child(type.nodeName)
        )
        callback(result.toSuspendFuncStatusInfo())
        return result
    }

    /**
     * もっと柔軟にしたいけど、とりあえずはベタ打ち
     */
    suspend fun getMailboxExtractionMailTypeCategoryAssignmentRef(
        type: MailboxExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): FetchResult<DatabaseReference> {
        val baseRefRet = getMailboxExtractionMailTypeSettingSingleRef(type, callback);
        if (baseRefRet !is FetchResult.Success) {
            callback(baseRefRet.toSuspendFuncStatusInfo())
            return baseRefRet
        }

        val baseRef = baseRefRet.data
        val kClass = getMailboxExtractionInternalClass(type)
        when (kClass) {
            MailboxExtraction::RakutenPay::class -> {
                val result = FetchResult.Success(
                    baseRef.child("storeCategoryAssignments")
                )
                return result
            }

            MailboxExtraction::AmazonItem::class -> {
                val result = FetchResult.Success(
                    baseRef.child("itemCategoryAssignments")
                )
                return result
            }

            else -> {
                val result = FetchResult.Failure.GenericFailure(
                    status = SuspendFuncStatus.FAILED,
                    errorMessage = "対応していないタイプです"
                )
                return result
            }
        }
    }
}