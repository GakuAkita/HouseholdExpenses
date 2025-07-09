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
import gaku.original.myapplication.data.toFetchResult
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
        var ref: DatabaseReference? = null
        try {
            withTimeout(2000) {
                withContext(Dispatchers.IO) {
                    currentUserId?.let {
                        val userId = currentUserId ?: ""
                        ref = database.child("users").child(userId)
                        //currentUserIdがnullかチェックしているので問題ない
                        val statusInfo = SuspendFuncStatusInfo(
                            status = SuspendFuncStatus.SUCCESS,
                            errorMessage = ""
                        )
                        callback(statusInfo)
                    } ?: {
                        Log.d(className, "userId is null")
                        throw Exception("userId is null")
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            LogTimeout(className, funcName, e)
            val statusInfo = SuspendFuncStatusInfo(
                status = SuspendFuncStatus.TIMEOUT,
                errorMessage = "Timeout : ${e.message}"
            )
            callback(statusInfo)
            return statusInfo.toFetchResult()
        } catch (e: Exception) {
            LogException(className, funcName, e)
            val statusInfo = SuspendFuncStatusInfo(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "${e.message}"
            )
            callback(statusInfo)
            return statusInfo.toFetchResult()
        }

        return FetchResult(
            status = SuspendFuncStatus.SUCCESS,
            errorMessage = "",
            data = ref
        )
    }

    /* getUserExpensesRefとgetUserCategoryRefで同じことをやっていたので共通化 */
    private suspend fun getUserChildrenRef(
        childrenPath: List<String>,/* たどり着きたい順に名前をいれていく */
        funcName: String,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): FetchResult<DatabaseReference> {
        var ref: DatabaseReference? = null

        val userRefRet = getUserRef()
        if (userRefRet.status != SuspendFuncStatus.SUCCESS || userRefRet.data == null) {
            callback(userRefRet.toSuspendFuncStatusInfo())
            return userRefRet
        }

        val userRef = userRefRet.data

        //シーケンスみたい。ある処理が終えたら次をスタートして、、みたいな。
        try {
            withTimeout(2000) {
                withContext(Dispatchers.IO) {
                    var tmp_ref = userRef//nullでない

                    childrenPath.forEach { childName ->
                        tmp_ref?.let {//null出ない場合
                            tmp_ref = tmp_ref?.child(childName)//nullでないことが保証されている
                        } ?: run {//nullのとき
                            throw Exception("tmp_ref became null childName:${childName}")
                            /* ここでループ自体は抜けてしたでcatchされるので、わざわざbreakしなくてよい */
                        }
                    }
                    ref = tmp_ref
                }
            }
        } catch (e: TimeoutCancellationException) {
            LogTimeout(className, funcName, e)
            val statusInfo = SuspendFuncStatusInfo(
                status = SuspendFuncStatus.TIMEOUT,
                errorMessage = "Timeout : ${e.message}"
            )
            callback(statusInfo)
            return statusInfo.toFetchResult()
        } catch (e: Exception) {
            LogException(className, funcName, e)
            val statusInfo = SuspendFuncStatusInfo(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "${e.message}"
            )
            callback(statusInfo)
            return statusInfo.toFetchResult()
        }

        return FetchResult(
            status = SuspendFuncStatus.SUCCESS,
            errorMessage = "",
            data = ref
        )
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
        var ref: DatabaseReference? = null
        val childrenPath = listOf("settings", "repeatAdd")

        val ret = getUserChildrenRef(childrenPath, funcName, callback)

        return ret
    }

    /* MailboxExtraction配下 */
    suspend fun getMailboxExtractionRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): FetchResult<DatabaseReference> {
        val funcName = ::getMailboxExtractionRef.name
        Log.d(className, "${funcName} was called")
        val childrenPath = listOf("mailbox_extraction")

        val ret = getUserChildrenRef(childrenPath, funcName, callback)
        return ret
    }

    suspend fun getMailboxExtractionMailTypeSettingsRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): FetchResult<DatabaseReference> {
        val funcName = ::getMailboxExtractionRef.name
        Log.d(className, "${funcName} was called")

        val baseRefRet = getMailboxExtractionRef()
        if (baseRefRet.status != SuspendFuncStatus.SUCCESS) {
            callback(baseRefRet.toSuspendFuncStatusInfo())
            return baseRefRet
        }

        val baseRef = baseRefRet.data
        if (baseRef == null) {

        }
        return ref
    }

    suspend fun getMailboxExtractionMailTypeSettingSingleRef(
        type: MailboxExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): DatabaseReference? {
        val funcName = ::getMailboxExtractionRef.name
        Log.d(className, "${funcName} was called")
        var ref: DatabaseReference? = null
        ref = getMailboxExtractionMailTypeSettingsRef(callback)?.child(type.nodeName)
        return ref
    }

    /**
     * もっと柔軟にしたいけど、とりあえずはベタ打ち
     */
    suspend fun getMailboxExtractionMailTypeCategoryAssignmentRef(
        type: MailboxExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): DatabaseReference? {
        val baseRef = getMailboxExtractionMailTypeSettingSingleRef(type, callback);
        if (baseRef == null) {
            return null
        }
        val kClass = getMailboxExtractionInternalClass(type)
        when (kClass) {
            MailboxExtraction::RakutenPay::class -> {
                return baseRef.child("storeCategoryAssignments")
            }

            MailboxExtraction::ShikokuElectricPower::class -> {
                return baseRef
            }

            MailboxExtraction::AmazonKindle::class -> {
                return baseRef
            }

            MailboxExtraction::AmazonItem::class -> {
                return baseRef.child("itemCategoryAssignments")
            }

            else -> {
                return null
            }
        }
    }
}