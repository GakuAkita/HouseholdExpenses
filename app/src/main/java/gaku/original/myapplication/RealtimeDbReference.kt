package gaku.original.myapplication

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import gaku.original.myapplication.viewModel.UserInfoViewModel
import javax.inject.Inject


class RealtimeDbReference @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
){
    private val database = Firebase.database.reference//users配下にそれぞれのuserIdが存在

    private val currentUserId :String?
        get() = firebaseAuth.currentUser?.uid
    //users配下の自分のuserIdのreferenceを返す
    // userId配下のexpenses
    fun getUserRef(): DatabaseReference {
        val userId = currentUserId ?: throw IllegalStateException("currentUserId is null")
        return database.child("users").child(userId)
    }

    // userId配下のexpenses
    fun getUserExpenseRef(): DatabaseReference {
        val userId = currentUserId ?: throw IllegalStateException("currentUserId is null")
        return database.child("users").child(userId).child("data").child("expenses")
    }

    //userId配下のcategory
    fun getUserCategoryRef(): DatabaseReference {
        val userId = currentUserId ?: throw IllegalStateException("currentUserId is null")
        return database.child("users").child(userId).child("data").child("categories")
    }
}