package gaku.original.myapplication.data

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await

class ExpenseRepository {
    private val database = Firebase.database.reference//users配下にそれぞれのuserIdが存在

    // ユーザーIDに基づいた共通の参照を事前に作成
    private fun getUSerExpenseRef(userId: String): DatabaseReference {
        return database.child("users").child(userId).child("data").child("expenses")
    }

    private fun getUserCategoryRef(userId:String):DatabaseReference {
        return database.child("users").child(userId).child("data").child("categories")
    }


    // ユーザーIDに基づいてデータをリストとして返す（非同期）
    suspend fun getExpenses(userId: String): List<Expense> {
        return try {
            val snapshot = getUSerExpenseRef(userId).get().await()  // 非同期でデータを取得
            snapshot.children.mapNotNull { it.getValue(Expense::class.java) }
        } catch (e: Exception) {
            emptyList()  // エラー時には空のリストを返す
        }
    }

    //経費を追加
    fun addExpense(userId:String, expense:Expense): Task<Void>{
        val expenseRef = getUSerExpenseRef(userId).push()
        return expenseRef.setValue(expense)
    }
}
