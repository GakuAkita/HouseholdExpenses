package gaku.original.myapplication

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import javax.inject.Inject


/* @TODO throwを出してしまうとアプリがクラッシュするらしい。したがって、nullのときの回避策を作る必要ある。*/
class RealtimeDbReference @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    private val database = Firebase.database.reference//users配下にそれぞれのuserIdが存在

    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    //users配下の自分のuserIdのreferenceを返す
    // userId配下のexpenses
//    suspend fun getUserRef(): DatabaseReference? {
//        var ref: DatabaseReference? = null
//        try {
//            withTimeout(2000) {
//                val userId = currentUserId ?: throw IllegalStateException("currentUserId is null")
//                ref = database.child("users").child(userId)
//            }
//        } catch (e: TimeoutCancellationException) {
//            Log.d("RealtimeDbReference", "getUserRef timeout")
//        } catch (e: Exception) {
//            Log.d("RealtimeDbReference", "UnexpectedError occurred in getUserRef.${e.message}")
//        }
//
//        return ref
//    }
    fun getUserRef(): DatabaseReference {
        val userId = currentUserId ?: throw IllegalStateException("currentUserId is null")
        return database.child("users").child(userId)
    }

    /** 本当はsuspendにした方が良いが、一旦避ける
    // userId配下のexpenses
    suspend fun getUserExpenseRef(): DatabaseReference {
    Log.d("RealtimeDbReference", "getUserExpenseRef was called.")
    var ref: DatabaseReference = database
    try {
    val result = withTimeout(2000) {
    ref = getUserRef().child("data").child("expenses")
    }
    } catch (e: TimeoutCancellationException) {
    Log.d("RealtimeDbReference", "getUserExpenseRef timeout")
    } catch (e: Exception) {
    Log.d(
    "RealtimeDbReference",
    "UnexpectedError occurred. in getUserExpenseRef.${e.message}"
    )
    }

    return ref
    }*/

    // userId配下のexpenses
    fun getUserExpenseRef(): DatabaseReference {
        Log.d("RealtimeDbReference", "getUserExpenseRef was called.")
        return getUserRef().child("data").child("expenses")
    }

    //userId配下のcategory
    fun getUserCategoryRef(): DatabaseReference {
        return getUserRef().child("data").child("categories")
    }

    fun getUserSettingsRef(): DatabaseReference {
        return getUserRef().child("settings")
    }

    fun getUserRepeatAddRef(): DatabaseReference {
        return getUserSettingsRef().child("repeatAdd")
    }
}