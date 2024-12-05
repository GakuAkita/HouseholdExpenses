package gaku.original.myapplication.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await

class ExpenseRepository  {
    private val database = Firebase.database.reference//users配下にそれぞれのuserIdが存在

    //users配下の自分のuserIdのreferenceを返す
    // userId配下のexpenses
    private fun getUserRef(userId: String): DatabaseReference {
        return database.child("users").child(userId)
    }

    // userId配下のexpenses
    private fun getUserExpenseRef(userId: String): DatabaseReference {
        return database.child("users").child(userId).child("data").child("expenses")
    }

    //userId配下のcategory
    private fun getUserCategoryRef(userId:String):DatabaseReference {
        return database.child("users").child(userId).child("data").child("categories")
    }

    //Realtime Databaseの差分だけ監視
    fun observeExpenses(userId:String,onExpenseChanged:(List<Expense>)->Unit) {
        val expenseRef = getUserExpenseRef(userId)
        expenseRef.addChildEventListener(object: ChildEventListener{
            private val expenses = mutableListOf<Expense>()

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val expense = snapshot.getValue(Expense::class.java)
                expense?.let{
                    expenses.add(it)
                    Log.d("ExpenseRepository","${it} added.")
                }
                onExpenseChanged(expenses)
            }

            override fun onChildChanged(snapshot:DataSnapshot,previousChildName:String?){
                val updatedExpense = snapshot.getValue(Expense::class.java)
                updatedExpense?.let{
                    expenses.replaceAll { if (it.id == updatedExpense.id) updatedExpense else it}
                    onExpenseChanged(expenses)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removedExpense = snapshot.getValue(Expense::class.java)
                removedExpense?.let{
                    expenses.removeIf { it.id == removedExpense.id}
                    onExpenseChanged(expenses)
                }
            }

            override fun onChildMoved(snapshot:DataSnapshot,previousChildName:String?){}

            override fun onCancelled(error: DatabaseError){}
        })
    }


    fun addUserInitialData(userId: String,email:String){
        val userRef = getUserRef(userId)
        userRef.child("email").setValue(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ExpenseRepository","addUserInitialData successful")
                }else{
                    Log.e("ExpenseRepository", "Failed to add initialData", task.exception)
                }
            }
    }


    // ユーザーIDに基づいてデータをリストとして返す（非同期）
    suspend fun fetchUserExpenses(userId: String): List<Expense> {
        try {
            val snapshot = getUserExpenseRef(userId).get().await()
            Log.d("ExpenseRepository", "fetchUserExpenses successful")
            val expenses = snapshot.children.mapNotNull {
                it.getValue(Expense::class.java)
            }
            Log.d("ExpenseRepository", "Fetched Expenses: $expenses")
            return expenses
        }catch (e: Exception) {
            Log.d("ExpenseRepository","fetchUserExpenses failed. ${e.message}")
            return emptyList()  // エラー時には空のリストを返す
        }
    }

    //経費を追加
    fun addExpense(userId:String, expense:Expense): Task<Void>{
        val expenseRef = getUserExpenseRef(userId).push()
        return expenseRef.setValue(expense)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ExpenseRepository", "Expense added successfully")
                } else {
                    Log.e("ExpenseRepository", "Failed to add expense", task.exception)
                }
            }
    }
}
