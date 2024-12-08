package gaku.original.myapplication.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await

class ExpenseRepository {
    private val database = Firebase.database.reference//users配下にそれぞれのuserIdが存在

    //users配下の自分のuserIdのreferenceを返す
    // userId配下のexpenses
    private fun getUserRef(userId: String): DatabaseReference {
        return database.child("users").child(userId)
    }

    // userId配下のexpenses
    private fun getUserExpenseRef(userId: String): DatabaseReference {
        return getUserRef(userId).child("data").child("expenses")
    }

    //userId配下のcategory
    private fun getUserCategoryRef(userId: String): DatabaseReference {
        return getUserRef(userId).child("data").child("categories")
    }

    //デバイスごとにタイムスタンプを管理
    private fun getDevicesRef(userId: String): DatabaseReference {
        return getUserRef(userId).child("devices")
    }

    //lastFetchedTimeの管理はRepository内で行い、UIで表示したかったらViewModelで読み出す
    private val _lastFetchedTime = MutableLiveData(0L)
    val lastFetchedTime: LiveData<Long> get() = _lastFetchedTime

    fun setLastFetchedTime(userId: String, deviceId: String,callback: (Long) -> Unit) {
        val deviceRef: DatabaseReference = getDevicesRef(userId).child(deviceId)

        // データを非同期で取得
        deviceRef.child("lastFetchedTime").get().addOnSuccessListener { dataSnapshot ->
            // データが存在する場合、取得したタイムスタンプを返す
            //SignUpした直後は0Lが入るかも。
            //Todo:オフライン対応が問題だな。今はずっとオンライン前提で考えている
            val timestamp = dataSnapshot.getValue(Long::class.java)?:0L
            _lastFetchedTime.postValue(timestamp)
            callback(timestamp)
        }.addOnFailureListener { exception ->
            // エラーが発生した場合
            Log.e("ExpenseRepository", "Error getting last fetched time", exception)
            //エラー時は何もしたくない。
            //0Lのときはオフラインなので、オンラインになった後でもlastFetchedTimeを更新したくない。
            //上でオンライン前提って考えているのにワロタ。
            callback(0L)
        }
    }

    fun updateLastFetchedTime(userId: String, deviceId: String, lastFetchedTime: Long,callback: (Boolean) -> Unit={}) {
        val deviceRef: DatabaseReference = getDevicesRef(userId).child(deviceId)
        deviceRef.child("lastFetchedTime").setValue(lastFetchedTime)
            .addOnSuccessListener {
                //成功した場合
                callback(true)
            }
            .addOnFailureListener { exception ->
                //失敗した場合
                Log.e("ExpenseRepository", "Error updating last fetched time", exception)
                callback(false)
            }
    }

    //Realtime Databaseの差分だけ監視
    fun observeExpenses(
        userId: String,
        deviceId: String,
        onExpenseAdded: (Expense) -> Unit,
        onExpenseUpdated: (Expense) -> Unit,
        onExpenseRemoved: (Expense) -> Unit
    ) {
        /*
        現状だと、query...startAt(..)の中身を動的に変えることはできないらしい。
        つまり、最初に実行された値で固定されてしまう。
        したがって、毎回リスナーを解除して、新たにリスナーを登録するって動きになるっぽい。
        なんとかもっと効率的にできないかな～
        */
        // LiveData を監視
        lastFetchedTime.observeForever { initialLastFetchedTime ->
            val expenseRef = getUserExpenseRef(userId)
            var lastFetchedTime = initialLastFetchedTime

            // クエリの変更前に前のリスナーを削除する
            var currentListener: ChildEventListener? = null

            // 初回クエリを実行
            val query = expenseRef.orderByChild("timestamp").startAt(lastFetchedTime.toDouble())

            // 新しいリスナーを作成
            val listener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val expense = snapshot.getValue(Expense::class.java)
                    expense?.let {
                        // `lastFetchedTime` を更新
                        lastFetchedTime = it.timestamp ?: lastFetchedTime
                        //これ+1しないと終わる。ずっと同じExpenseを追加することになる。
//                        _lastFetchedTime.postValue(lastFetchedTime+1) // LiveData を更新
                        updateLastFetchedTime(
                            userId,
                            deviceId,
                            lastFetchedTime+1,
                            callback = {}
                            )
                        onExpenseAdded(it)
                        Log.d("ExpenseRepository", "Updated lastFetchedTime to: $lastFetchedTime")
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val updatedExpense = snapshot.getValue(Expense::class.java)
                    updatedExpense?.let {
                        onExpenseUpdated(it)
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val removedExpense = snapshot.getValue(Expense::class.java)
                    removedExpense?.let {
                        onExpenseRemoved(it)
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            }

            // 最初のリスナーを登録
            query.addChildEventListener(listener)
            currentListener = listener

            // もしlastFetchedTimeが変更された場合、その後にデータを再取得する
            _lastFetchedTime.observeForever { newFetchedTime ->
                if (newFetchedTime != lastFetchedTime) {
                    // 古いリスナーを削除
                    currentListener?.let { query.removeEventListener(it) }

                    // 新しいタイムスタンプでクエリを再実行
                    val newQuery = expenseRef.orderByChild("timestamp").startAt(newFetchedTime.toDouble())
                    // 新しいリスナーを追加
                    newQuery.addChildEventListener(listener)
                    currentListener = listener // 更新されたリスナーを保持

                    lastFetchedTime = newFetchedTime
                }
            }
        }
    }


    fun addUserInitialData(userId: String, email: String) {
        val userRef = getUserRef(userId)
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
    suspend fun fetchUserExpenses(userId: String): List<Expense> {
        try {
            val snapshot = getUserExpenseRef(userId).get().await()
            Log.d("ExpenseRepository", "fetchUserExpenses successful")
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

    fun addExpense(userId: String, expense: Expense) {
        val expenseRef = getUserExpenseRef(userId)
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
        val expenseRef = getUserExpenseRef(userId)

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
}
