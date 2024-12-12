package gaku.original.myapplication

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class RealtimeDbReference(
    private val userId:String
){
    private val database = Firebase.database.reference//users配下にそれぞれのuserIdが存在

    //users配下の自分のuserIdのreferenceを返す
    // userId配下のexpenses
    fun getUserRef(): DatabaseReference {
        return database.child("users").child(userId)
    }

    // userId配下のexpenses
    fun getUserExpenseRef(): DatabaseReference {
        return database.child("users").child(userId).child("data").child("expenses")
    }

    //userId配下のcategory
    fun getUserCategoryRef(): DatabaseReference {
        return database.child("users").child(userId).child("data").child("categories")
    }
}