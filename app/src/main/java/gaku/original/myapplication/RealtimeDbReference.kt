package gaku.original.myapplication

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import gaku.original.myapplication.Utility.LogTimeout
import gaku.original.myapplication.Utility.LogUnexpectedError
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import javax.inject.Inject


/* @TODO throwを出してしまうとアプリがクラッシュするらしい。したがって、nullのときの回避策を作る必要ある。*/
class RealtimeDbReference @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    val className: String = this::class.simpleName ?: "UnableToGetClassName"

    private val database = Firebase.database.reference//users配下にそれぞれのuserIdが存在

    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    //users配下の自分のuserIdのreferenceを返す
    // userId配下のexpenses
    suspend fun getUserRef(callback: (SuspendFuncStatus) -> Unit = {}): DatabaseReference? {
        val funcName = ::getUserRef.name
        var ref: DatabaseReference? = null
        try {
            withTimeout(2000) {
                if (currentUserId != null) {
                    val userId = currentUserId ?: ""
                    ref = database.child("users").child(userId)
                    //currentUserIdがnullかチェックしているので問題ない
                    callback(SuspendFuncStatus.SUCCESS)
                } else {
                    Log.d(className, "userId is null")
                    callback(SuspendFuncStatus.FAILED)
                }
            }
        } catch (e: TimeoutCancellationException) {
            LogTimeout(className, funcName, e)
            callback(SuspendFuncStatus.TIMEOUT)
        } catch (e: Exception) {
            LogUnexpectedError(className, funcName, e)
            callback(SuspendFuncStatus.FAILED)
        }

        return ref
    }

    /* getUserExpensesRefとgetUserCategoryRefで同じことをやっていたので共通化 */
    suspend fun getUserDataChildRef(
        childPath: String,
        funcName: String,
        callback: (SuspendFuncStatus) -> Unit = {}
    ): DatabaseReference? {
        var ref: DatabaseReference? = null
        try {
            //こっちのタイムアウトはgetUserRefのタイムアウトよりも長くしておく必要ある？
            withTimeout(3000) {
                val userRef = getUserRef() { status ->
                    if (status == SuspendFuncStatus.SUCCESS) {
                        /* Do nothing */
                    } else if (status == SuspendFuncStatus.TIMEOUT) {
                        //getUserRefのタイムアウトとgetUserExpenseRefのタイムアウトが区別つくのか？？
                        callback(SuspendFuncStatus.TIMEOUT)
                    } else {
                        callback(SuspendFuncStatus.FAILED)
                    }
                }

                if (userRef != null) {
                    ref = userRef.child("data").child(childPath)
                    //ここまで来て初めて成功
                    callback(SuspendFuncStatus.SUCCESS)
                } else {
                    Log.d(className, "${funcName} ended successfully, but null${userRef}")
                    callback(SuspendFuncStatus.FAILED)
                }
            }
        } catch (e: TimeoutCancellationException) {
            LogTimeout(className, funcName, e)
            callback(SuspendFuncStatus.TIMEOUT)
        } catch (e: Exception) {
            LogUnexpectedError(className, funcName, e)
            callback(SuspendFuncStatus.FAILED)
        }
        return ref
    }

    // userId配下のexpenses
    suspend fun getUserExpenseRef(callback: (SuspendFuncStatus) -> Unit = {}): DatabaseReference? {
        val funcName = ::getUserExpenseRef.name
        Log.d(className, "${funcName} was called.")
        var ref: DatabaseReference? = null

        try {
            ref = getUserDataChildRef("expenses", funcName, callback)
        } catch (e: Exception) {
            /* 引数のcallbackに何をやるかいれる */
        }
        return ref
    }

    //userId配下のcategory
    suspend fun getUserCategoryRef(callback: (SuspendFuncStatus) -> Unit = {}): DatabaseReference? {
        val funcName = ::getUserCategoryRef.name
        Log.d(className, "${funcName} was called")
        var ref: DatabaseReference? = null
        try {
            ref = getUserDataChildRef("categories", funcName, callback)
        } catch (e: Exception) {
            /* 引数のcallbackに何をやるかいれる */
        }

        return ref
    }

    fun getUserSettingsRef(): DatabaseReference {
        return getUserRef().child("settings")
    }

    fun getUserRepeatAddRef(): DatabaseReference {
        return getUserSettingsRef().child("repeatAdd")
    }
}