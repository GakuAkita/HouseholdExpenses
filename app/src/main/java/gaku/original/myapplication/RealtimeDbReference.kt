package gaku.original.myapplication

import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import gaku.original.myapplication.viewModel.UserInfoViewModel

class RealtimeDbReference(private val userInfoViewModel: UserInfoViewModel

){
    private val database = Firebase.database.reference//users配下にそれぞれのuserIdが存在

    private val currentUserId :String
        get() = userInfoViewModel.getUserId()
    //users配下の自分のuserIdのreferenceを返す
    // userId配下のexpenses
    fun getUserRef(): DatabaseReference {
        return database.child("users").child(currentUserId)
    }

    // userId配下のexpenses
    fun getUserExpenseRef(): DatabaseReference {
        return database.child("users").child(currentUserId).child("data").child("expenses")
    }

    //userId配下のcategory
    fun getUserCategoryRef(): DatabaseReference {
        return database.child("users").child(currentUserId).child("data").child("categories")
    }
}