package gaku.original.myapplication

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.MailboxExtractionCommon
import gaku.original.myapplication.utility.LogException
import gaku.original.myapplication.utility.LogTimeout
import kotlinx.coroutines.TimeoutCancellationException
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
    suspend fun getUserRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): DatabaseReference? {
        val funcName = ::getUserRef.name
        var ref: DatabaseReference? = null
        try {
            withTimeout(2000) {
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
        } catch (e: TimeoutCancellationException) {
            LogTimeout(className, funcName, e)
            val statusInfo = SuspendFuncStatusInfo(
                status = SuspendFuncStatus.TIMEOUT,
                errorMessage = "Timeout : ${e.message}"
            )
            callback(statusInfo)
        } catch (e: Exception) {
            LogException(className, funcName, e)
            val statusInfo = SuspendFuncStatusInfo(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "${e.message}"
            )
            callback(statusInfo)
        }

        return ref
    }

    /* getUserExpensesRefとgetUserCategoryRefで同じことをやっていたので共通化 */
    private suspend fun getUserChildrenRef(
        childrenPath: List<String>,/* たどり着きたい順に名前をいれていく */
        funcName: String,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): DatabaseReference? {
        var ref: DatabaseReference? = null

        val userRef = getUserRef { status ->
            if (status.status != SuspendFuncStatus.SUCCESS) {//成功のときはスルー
                callback(status)
            }
        }//これで待ってくれる

        if (userRef == null) {
            return ref
        }

        //シーケンスみたい。ある処理が終えたら次をスタートして、、みたいな。
        try {
            withTimeout(2000) {
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
        } catch (e: TimeoutCancellationException) {
            LogTimeout(className, funcName, e)
            val statusInfo = SuspendFuncStatusInfo(
                status = SuspendFuncStatus.TIMEOUT,
                errorMessage = "Timeout : ${e.message}"
            )
            callback(statusInfo)
        } catch (e: Exception) {
            LogException(className, funcName, e)
            val statusInfo = SuspendFuncStatusInfo(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "${e.message}"
            )
            callback(statusInfo)
        }
        return ref
    }

    // userId配下のexpenses
    suspend fun getUserExpenseRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): DatabaseReference? {
        val funcName = ::getUserExpenseRef.name
        Log.d(className, "${funcName} was called.")
        var ref: DatabaseReference? = null
        val childrenPath = listOf("data", "expenses")

        ref = getUserChildrenRef(childrenPath, funcName, callback)

        return ref
    }

    //userId配下のcategory
    suspend fun getUserCategoryRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): DatabaseReference? {
        val funcName = ::getUserCategoryRef.name
        Log.d(className, "${funcName} was called")
        var ref: DatabaseReference? = null
        val childrenPath = listOf("data", "categories")

        ref = getUserChildrenRef(childrenPath, funcName, callback)

        return ref
    }

    suspend fun getUserSettingsRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): DatabaseReference? {
        val funcName = ::getUserSettingsRef.name
        Log.d(className, "${funcName} was called")
        var ref: DatabaseReference? = null
        val childrenPath = listOf("settings")

        ref = getUserChildrenRef(childrenPath, funcName, callback)

        return ref
    }

    suspend fun getUserRepeatAddRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): DatabaseReference? {
        val funcName = ::getUserRepeatAddRef.name
        Log.d(className, "${funcName} was called")
        var ref: DatabaseReference? = null
        val childrenPath = listOf("settings", "repeatAdd")

        ref = getUserChildrenRef(childrenPath, funcName, callback)

        return ref
    }

    /* MailboxExtraction配下 */
    suspend fun getMailboxExtractionRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): DatabaseReference? {
        val funcName = ::getMailboxExtractionRef.name
        Log.d(className, "${funcName} was called")
        var ref: DatabaseReference? = null
        val childrenPath = listOf("mailbox_extraction")

        ref = getUserChildrenRef(childrenPath, funcName, callback)

        return ref
    }

    suspend fun getMailboxExtractionMailTypeSettingsRef(callback: (SuspendFuncStatusInfo) -> Unit = {}): DatabaseReference? {
        val funcName = ::getMailboxExtractionRef.name
        Log.d(className, "${funcName} was called")
        var ref: DatabaseReference? = null
        ref = getMailboxExtractionRef(callback)?.child("mail_type_settings")

        return ref
    }

    suspend fun getMailboxExtractionMailTypeSettingSingleRef(
        type: MailboxExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): DatabaseReference? {
        val funcName = ::getMailboxExtractionRef.name
        Log.d(className, "${funcName} was called")
        var ref: DatabaseReference? = null
        ref = getMailboxExtractionMailTypeSettingsRef(callback)?.child(type.documentName)
        return ref
    }
}