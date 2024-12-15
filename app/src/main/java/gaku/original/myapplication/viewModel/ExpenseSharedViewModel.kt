package gaku.original.myapplication.viewModel

import gaku.original.myapplication.DbListenerManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ExpenseSharedViewModel(
    private val expenseRepository: ExpenseRepository,
    private val dbListenerManager: DbListenerManager
):ViewModel() {
    //@TODO 総データ量が多くないので、データをすべて引っ張ってくる仕様だが、将来的には数ヶ月分だけとってくる形にする
    private val _allExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val allExpense: StateFlow<List<Expense>> get() = _allExpenses

    //realtimeDbReferenceからとっても良いが、引数が増えるのでdbListenerManagerから取る
    private val expenseRef: DatabaseReference
        get() = dbListenerManager.expenseRef

    //こっちはある時間以降の変更しか見ない
    private val listAddChildEventListener = object: ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("ExpenseSharedViewModel", "onChildAdded was called.")
            val newExpense = snapshot.getValue(Expense::class.java)
            newExpense?.let {
                viewModelScope.launch {
                    Log.d("ExpenseSharedViewModel", "_allExpenses.value size: ${_allExpenses.value.size}")
                    _allExpenses.value += newExpense
                    Log.d("ExpenseSharedViewModel", "Expense added: $newExpense")
                }
            }
        }
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
    }

    //変更されたときや取り除かれたとき常に監視する
    private val listWatchChildEventListener = object: ChildEventListener {

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("ExpenseSharedViewModel", "onChildChanged was called.")
            val updatedExpense = snapshot.getValue(Expense::class.java)
            updatedExpense?.let {
                viewModelScope.launch {
                    _allExpenses.value = _allExpenses.value.map { expense ->
                        if (expense.id == updatedExpense.id){
                            updatedExpense
                        } else {
                            expense
                        }
                    }
                    Log.d("ExpenseSharedViewModel", "Expense updated: ${updatedExpense.id}")
                }
            }
        }
        override fun onChildRemoved(snapshot: DataSnapshot) {
            Log.d("ExpenseSharedViewModel", "onChildRemoved was called.")
            val removedExpense = snapshot.getValue(Expense::class.java)
            removedExpense?.let{
                viewModelScope.launch {
                    _allExpenses.value = _allExpenses.value.filterNot { expense ->
                        expense.id == removedExpense.id
                    }
                    Log.d("ExpenseSharedViewModel", "Expense removed: $removedExpense")
                }
            }
        }
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
    }

    //サインインしたタイミングで実行する
    fun addExpenseChildEventListener() {
        //実行されたタイミングのtimeだけあればよい。
        val firstFetchedTime = System.currentTimeMillis()
        val queryForAdded = expenseRef.orderByChild("timestamp").startAt(firstFetchedTime.toDouble())

        //リスナーを追加
        dbListenerManager.addListener(queryForAdded,listAddChildEventListener)
        dbListenerManager.addListener(expenseRef,listWatchChildEventListener)

        //リスナーが溜まっているかどうかは、UIに表示してみればよいか。
    }

    fun clearExpenseChildEventListener(){
        dbListenerManager.removeAllListeners()
    }

    /*******************CRUD関連**************************/
    fun fetchAllExpenses(onComplete:()->Unit={}){
        viewModelScope.launch {
            _allExpenses.value = expenseRepository.fetchUserExpenses()
            Log.d("ExpenseViewModel","Expenses:${_allExpenses.value}")
            onComplete()
        }
    }

    fun addExpense(expense: Expense){
        //idはpushしたときに代入することにする。したがって、nullのままにする。
        //repositoryのaddExpenseでidを格納する
        if(expense.category == null){
            expense.category = ""
        }
        if(expense.note == null){
            expense.note = ""
        }
        viewModelScope.launch {
            expenseRepository.addExpense(expense)
        }
    }

    fun updateExpense(expense:Expense){
        viewModelScope.launch {
            expenseRepository.updateExpense(expense)
        }
    }

    fun removeExpense(expense:Expense){
        viewModelScope.launch {
            expenseRepository.removeExpense(expense)
        }
    }
}