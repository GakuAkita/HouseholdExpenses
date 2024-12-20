package gaku.original.myapplication.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.DbListenerManager
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.CategoryRepository
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExpenseSharedViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val dbListenerManager: DbListenerManager
):ViewModel() {
    //@TODO 総データ量が多くないので、データをすべて引っ張ってくる仕様だが、将来的には数ヶ月分だけとってくる形にする
    private val _allExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val allExpenses: StateFlow<List<Expense>> get() = _allExpenses

    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
    val allCategories: StateFlow<List<Category>> get() = _allCategories

    //realtimeDbReferenceからとっても良いが、引数が増えるのでdbListenerManagerから取る
    private val expenseRef: DatabaseReference
        get() = dbListenerManager.expenseRef

    private val categoryRef: DatabaseReference
        get() = dbListenerManager.categoryRef

    //こっちはある時間以降の変更しか見ない
    private val expenseListAddChildEventListener = object: ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("ExpenseSharedViewModel", "onChildAdded(Expense) was called.")
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
    private val expenseListWatchChildEventListener = object: ChildEventListener {

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("ExpenseSharedViewModel", "onChildChanged(Expense) was called.")
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
            Log.d("ExpenseSharedViewModel", "onChildRemoved(Expense) was called.")
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
    fun addExpenseCategoryChildEventListener() {
        //実行されたタイミングのtimeだけあればよい。
        val firstFetchedTime = System.currentTimeMillis()
        val queryForAddedExpense = expenseRef.orderByChild("timestamp").startAt(firstFetchedTime.toDouble())
        val queryForAddedCategory = categoryRef.orderByChild("timestamp").startAt(firstFetchedTime.toDouble())

        //リスナーを追加
        //Expense用
        dbListenerManager.addListener(queryForAddedExpense,expenseListAddChildEventListener)
        dbListenerManager.addListener(expenseRef,expenseListWatchChildEventListener)

        //Category用
        dbListenerManager.addListener(queryForAddedCategory,categoryListAddChildEventListener)
        dbListenerManager.addListener(categoryRef,categoryListWatchChildEventListener)

        //リスナーが溜まっているかどうかは、UIに表示してみればよいか。
    }

    fun clearExpenseChildEventListener(){
        dbListenerManager.removeAllListeners()
    }

    /*******************Expense CRUD関連**************************/
    fun addUserInitialData(email:String){
        //呼び出すだけ。関数名が全く同じなので変えたほうが良いかも
        expenseRepository.addUserInitialData(email)

        val category = Category(
            id = null,
            name = "Food",
            enabled = true
        )
        categoryRepository.addCategory(category)
    }

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

    /*******************Category CRUD関連**************************/
    /* @TODO 将来的にはallExpensesと同じようなローカルに保持しておく。(カテゴリー自体は数が多くならないので今は毎回fetchする感じにする) */
    private val categoryListAddChildEventListener = object: ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("ExpenseSharedViewModel", "onChildAdded(Category) was called.")
            val newCategory = snapshot.getValue(Category::class.java)
            newCategory?.let {
                viewModelScope.launch {
                    Log.d("ExpenseSharedViewModel", "_allExpenses.value size: ${_allCategories.value.size}")
                    _allCategories.value += newCategory
                    Log.d("ExpenseSharedViewModel", "Expense added: $newCategory")
                }
            }
        }
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
    }

    //変更されたときや取り除かれたとき常に監視する
    private val categoryListWatchChildEventListener = object: ChildEventListener {

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("ExpenseSharedViewModel", "onChildChanged(Category) was called.")
            val updatedCategory = snapshot.getValue(Category::class.java)
            updatedCategory?.let {
                viewModelScope.launch {
                    _allCategories.value = _allCategories.value.map { category ->
                        if (category.id == updatedCategory.id){
                            updatedCategory
                        } else {
                            category
                        }
                    }
                    Log.d("ExpenseSharedViewModel", "Category updated: ${updatedCategory.id}")
                }
            }
        }
        override fun onChildRemoved(snapshot: DataSnapshot) {
            Log.d("ExpenseSharedViewModel", "onChildRemoved(Category) was called.")
            val removedExpense = snapshot.getValue(Expense::class.java)
            removedExpense?.let{
                viewModelScope.launch {
                    _allExpenses.value = _allExpenses.value.filterNot { expense ->
                        expense.id == removedExpense.id
                    }
                    Log.d("ExpenseSharedViewModel", "Category removed: $removedExpense")
                }
            }
        }
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
    }


    fun fetchAllCategories(){
        viewModelScope.launch {
            _allCategories.value = categoryRepository.fetchAllCategories()
            Log.d("CategoryViewModel","Categories:${_allCategories.value}")
        }
    }

    fun addCategory(category:Category, onAlreadyExists:()->Unit={}){
        //addするときに、既存のカテゴリーと重複しないようにする
        val isNameAlreadyExists = allCategories.value.any { it.name == category.name }
        if(isNameAlreadyExists){
            onAlreadyExists()
        }
        else {
            viewModelScope.launch {
                categoryRepository.addCategory(category)
            }
        }
    }

    fun updateCategory(category:Category){
        viewModelScope.launch {
            categoryRepository.updateCategory(category)
        }
    }

    fun removeCategory(category:Category){
        viewModelScope.launch {
            categoryRepository.removeCategory(category)
        }
    }
}