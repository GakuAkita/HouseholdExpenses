package gaku.original.myapplication.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ExpenseRepository @Inject constructor() {
    private val database = Firebase.database.reference//users配下にそれぞれのuserIdが存在

    // ユーザーIDに基づいた共通の参照を事前に作成
    private fun getUserExpenseRef(userId: String): DatabaseReference {
        return database.child("users").child(userId).child("data").child("expenses")
    }

    private fun getUserCategoryRef(userId:String):DatabaseReference {
        return database.child("users").child(userId).child("data").child("categories")
    }


    // ユーザーIDに基づいてデータをリストとして返す（非同期）
    suspend fun getExpenses(userId: String): List<Expense> {
        return try {
            val snapshot = getUserExpenseRef(userId).get().await()  // 非同期でデータを取得
            Log.d("ExpenseRepository","getExpenses successful")
            snapshot.children.mapNotNull { it.getValue(Expense::class.java) }
        } catch (e: Exception) {
            Log.d("ExpenseRepository","getExpenses failed")
            emptyList()  // エラー時には空のリストを返す
        }
    }

    //経費を追加
    fun addExpense(userId:String, expense:Expense): Task<Void>{
        val expenseRef = getUserExpenseRef(userId).push()
        return expenseRef.setValue(expense)
    }
}
