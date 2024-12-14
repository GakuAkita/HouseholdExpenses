package gaku.original.myapplication.data

import ListenerManager
import android.util.Log
import gaku.original.myapplication.RealtimeDbReference
import kotlinx.coroutines.tasks.await

class ExpenseRepository(
    private val realtimeDbReference: RealtimeDbReference
) {
//    fun observeExpenses(
//        userId: String,
//        lastFetchedTime: Long,
//        onExpenseAdded: (Expense) -> Unit,
//        onExpenseUpdated: (Expense) -> Unit,
//        onExpenseRemoved: (Expense) -> Unit
//    ) {
//        val expenseRef = getUserExpenseRef(userId)
//
//        // `onChildAdded` 用のクエリ
//        val addedQuery = expenseRef.orderByChild("timestamp").startAt(lastFetchedTime.toDouble())
//        Log.d("ExpenseRepository", "lastFetchedTime.toDouble(): ${lastFetchedTime.toDouble()}")
//
//        // `onChildAdded` のみ
//        addedQuery.addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                Log.d("ExpenseRepository", "onChildAdded was called.")
//                val expense = snapshot.getValue(Expense::class.java)
//                expense?.let { onExpenseAdded(it) }
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
//            override fun onChildRemoved(snapshot: DataSnapshot) {}
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
//        })
//
//        // onChildChangedとonChildRemovedはtimestampによらず監視をする。
//        expenseRef.addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                Log.d("ExpenseRepository", "onChildChanged was called.")
//                val updatedExpense = snapshot.getValue(Expense::class.java)
//                updatedExpense?.let { onExpenseUpdated(it) }
//            }
//
//            override fun onChildRemoved(snapshot: DataSnapshot) {
//                Log.d("ExpenseRepository", "onChildRemoved was called.")
//                val removedExpense = snapshot.getValue(Expense::class.java)
//                removedExpense?.let { onExpenseRemoved(it) }
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
//        })
//    }

    //SignUp後にやる操作
    fun addUserInitialData(email: String) {
        val userRef = realtimeDbReference.getUserRef()
        userRef.child("email").setValue(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ExpenseRepository", "addUserInitialData successful")
                } else {
                    Log.e("ExpenseRepository", "Failed to add initialData", task.exception)
                }
            }
    }


    // ユーザーIDに基づいてデータをリストとして返す（非同期）
    suspend fun fetchUserExpenses(): List<Expense> {
        try {
            val snapshot = realtimeDbReference.getUserExpenseRef().get().await()
            val expenses = snapshot.children.mapNotNull {
                it.getValue(Expense::class.java)
            }
            Log.d("ExpenseRepository", "Fetched Expenses: $expenses")
            return expenses
        } catch (e: Exception) {
            Log.d("ExpenseRepository", "fetchUserExpenses failed. ${e.message}")
            return emptyList()  // エラー時には空のリストを返す
        }
    }

    fun addExpense(expense: Expense) {
        val expenseRef = realtimeDbReference.getUserExpenseRef()
        val newExpenseRef = expenseRef.push() // Generate the unique key

        // Create a new instance of Expense with the generated ID
        val expenseWithId = expense.copy(id = newExpenseRef.key)

        // Save the new instance with the generated key
        newExpenseRef.setValue(expenseWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ExpenseRepository", "Expense added successfully")
                } else {
                    Log.e("ExpenseRepository", "Failed to add expense", task.exception)
                }
            }
    }

    fun updateExpense(userId: String, expense: Expense) {
        val expenseRef = realtimeDbReference.getUserExpenseRef()

        // Use the expense's ID (which is the Firebase-generated key) to locate it
        val expenseToUpdateRef = expenseRef.child(expense.id ?: return)

        expenseToUpdateRef.setValue(expense)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ExpenseRepository", "Expense updated successfully")
                } else {
                    Log.e("ExpenseRepository", "Failed to update expense", task.exception)
                }
            }
    }

    fun removeExpense(userId:String,expense:Expense){
        val expenseRef = realtimeDbReference.getUserExpenseRef()
        val expenseToRemoveRef = expenseRef.child(expense.id ?: return)
        expenseToRemoveRef.removeValue()
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Log.d("ExpenseRepository","Expense removed successfully")
                } else{
                    Log.e("ExpenseRepository","Failed to remove expense",task.exception)
                }
            }
    }
}
