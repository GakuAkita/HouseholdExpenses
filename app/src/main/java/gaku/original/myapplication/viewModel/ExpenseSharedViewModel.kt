package gaku.original.myapplication.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.DbListenerManager
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.CategoryRepository
import gaku.original.myapplication.data.Constants.Status.CategoryEditStatus
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.ExpenseRepository
import gaku.original.myapplication.data.InitialCategories
import gaku.original.myapplication.data.generatedType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExpenseSharedViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val dbListenerManager: DbListenerManager,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    //@TODO 総データ量が多くないので、データをすべて引っ張ってくる仕様だが、将来的には数ヶ月分だけとってくる形にする
    private val _allExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val allExpenses: StateFlow<List<Expense>> get() = _allExpenses

    //emptyList()のとき、Loadingがうまく行ってないのか、シンプルに何も保存されていないのかの区別がつかない
    private val _expensesLoadingStatus = MutableStateFlow(LoadingStatus.COMPLETED)
    val expensesLoadingStatus: StateFlow<LoadingStatus> get() = _expensesLoadingStatus

    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
    val allCategories: StateFlow<List<Category>> get() = _allCategories

    //realtimeDbReferenceから取っても良いが、引数が増えるのでdbListenerManagerから取る
    suspend fun getExpenseRef(callback: (SuspendFuncStatus) -> Unit = {}): DatabaseReference? {
        return dbListenerManager.getExpenseRef(callback)
    }

    suspend fun getCategoryRef(callback: (SuspendFuncStatus) -> Unit = {}): DatabaseReference? {
        return dbListenerManager.getCategoryRef(callback)
    }

    //こっちはある時間以降の変更しか見ない
    private val expenseListAddChildEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("ExpenseSharedViewModel", "onChildAdded(Expense) was called.")
            val newExpense = snapshot.getValue(Expense::class.java)
            newExpense?.let {
                viewModelScope.launch {
                    Log.d(
                        "ExpenseSharedViewModel",
                        "_allExpenses.value size: ${_allExpenses.value.size}"
                    )
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
    private val expenseListWatchChildEventListener = object : ChildEventListener {

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("ExpenseSharedViewModel", "onChildChanged(Expense) was called.")
            val updatedExpense = snapshot.getValue(Expense::class.java)
            updatedExpense?.let {
                viewModelScope.launch {
                    _allExpenses.value = _allExpenses.value.map { expense ->
                        if (expense.id == updatedExpense.id) {
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
            removedExpense?.let {
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
    suspend fun addExpenseCategoryChildEventListener(callback: (SuspendFuncStatus) -> Unit) {
        //実行されたタイミングのtimeだけあればよい。
        val firstFetchedTime = System.currentTimeMillis()

        getExpenseRef()?.let {
            //この中ではgetExpenseRefはnullでない
            val queryForAddedExpense =
                getExpenseRef()?.orderByChild("timestamp")?.startAt(firstFetchedTime.toDouble())
            //リスナーを追加
            //Expense用
            dbListenerManager.addListener(queryForAddedExpense, expenseListAddChildEventListener)
            dbListenerManager.addListener(getExpenseRef(), expenseListWatchChildEventListener)
        } ?: run {
            return
        }

        getCategoryRef()?.let {
            val queryForAddedCategory =
                getCategoryRef()?.orderByChild("timestamp")?.startAt(firstFetchedTime.toDouble())
            //Category用
            dbListenerManager.addListener(queryForAddedCategory, categoryListAddChildEventListener)
            dbListenerManager.addListener(getExpenseRef(), categoryListWatchChildEventListener)
        } ?: run {
            return
        }

        //リスナーが溜まっているかどうかは、UIに表示してみればよいか。

    }

    fun clearExpenseChildEventListener() {
        dbListenerManager.removeAllListeners()
    }

    /*******************Expense CRUD関連**************************/
    init {
        Log.d("ExpenseSharedViewModel", "Init was called.")

        /* これがないと、MainViewに遷移したときに_allExpensesに値が入らない */
        if (firebaseAuth.currentUser != null &&
            _allExpenses.value.isEmpty()
        ) {
            fetchAllExpenses(
                onStart = {
                    Log.d("AkitaDebug", "Set loadingstatus as loading")
                    _expensesLoadingStatus.value = LoadingStatus.LOADING
                },
                callback = { status ->
                    if (status == SuspendFuncStatus.SUCCESS) {
                        addExpenseCategoryChildEventListener()
                        _expensesLoadingStatus.value = LoadingStatus.COMPLETED
                        Log.d("AkitaDebug", "Set loadingStatus as Completed")
                    } else if (status == SuspendFuncStatus.TIMEOUT) {
                        _expensesLoadingStatus.value = LoadingStatus.TIMEOUT
                    } else {
                        _expensesLoadingStatus.value = LoadingStatus.ERROR
                        Log.d("AkitaDebug", "Set loadingStatus as Error")
                    }
                }
            )
        } else {
            Log.d("ExpenseSharedViewModel", "User is not signed in.")
        }

        if (firebaseAuth.currentUser != null &&
            _allCategories.value.isEmpty()
        ) {
            fetchAllCategories()
        }
    }

    fun addUserInitialData(email: String) {
        //呼び出すだけ。関数名が全く同じなので変えたほうが良いかも
        viewModelScope.launch {
            expenseRepository.addUserInitialData(email)
        }
        //デフォルトカテゴリーを追加
        for (defaultCategory in InitialCategories.categories) {
            categoryRepository.addCategory(defaultCategory)
        }
    }

    suspend fun fetchAllExpenses(
        onStart: () -> Unit = {},
        callback: (SuspendFuncStatus) -> Unit = {}
    ) {
        onStart()
        viewModelScope.launch {
            _allExpenses.value = expenseRepository.fetchUserExpenses(
                callback = callback
            )//@TODO エラー対策ができていないな。
            Log.d("ExpenseSharedViewModel", "Expenses:${_allExpenses.value}")
        }
    }

    fun addExpense(expense: Expense, callback: (Boolean) -> Unit = {}) {
        if (expense.generatedType == null) {
            expense.generatedType = generatedType.MANUAL
        }

        if (expense.note == null) {
            expense.note = ""
        }

        viewModelScope.launch {
            expenseRepository.addExpense(expense, callback)
        }
    }

    fun updateExpense(expense: Expense, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            expenseRepository.updateExpense(expense, callback)
        }
    }

    fun removeExpense(expense: Expense, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            expenseRepository.removeExpense(expense, callback)
        }
    }

    /*******************Category CRUD関連**************************/
    /* @TODO 将来的にはallExpensesと同じようなローカルに保持しておく。(カテゴリー自体は数が多くならないので今は毎回fetchする感じにする) */
    private val categoryListAddChildEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("ExpenseSharedViewModel", "onChildAdded(Category) was called.")
            val newCategory = snapshot.getValue(Category::class.java)
            newCategory?.let {
                viewModelScope.launch {
                    Log.d(
                        "ExpenseSharedViewModel",
                        "_allExpenses.value size: ${_allCategories.value.size}"
                    )
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
    private val categoryListWatchChildEventListener = object : ChildEventListener {

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("ExpenseSharedViewModel", "onChildChanged(Category) was called.")
            val updatedCategory = snapshot.getValue(Category::class.java)
            updatedCategory?.let {
                viewModelScope.launch {
                    _allCategories.value = _allCategories.value.map { category ->
                        if (category.id == updatedCategory.id) {
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
            removedExpense?.let {
                viewModelScope.launch {
                    _allCategories.value = _allCategories.value.filterNot { expense ->
                        expense.id == removedExpense.id
                    }
                    Log.d("ExpenseSharedViewModel", "Category removed: $removedExpense")
                }
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
    }


    fun fetchAllCategories(callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _allCategories.value = categoryRepository.fetchAllCategories(
                callback = { result ->
                    callback(result)
                }
            )
            Log.d("CategoryViewModel", "Categories:${_allCategories.value}")
        }
    }

    /*
    被りチェックはここで入れたほうが良いな。
    CategoryEditView用の関数を作りそこで被りチェックを入れても良いが、そうすると、
    SharedViewModelを他のviewModelでチェックしてCategoryを追加するときに同じ機能を実装することになる。
    */
    fun addCategory(category: Category, callback: (CategoryEditStatus) -> Unit = {}) {
        val isNameAlreadyExists = allCategories.value.any { it.name == category.name }
        if (isNameAlreadyExists) {
            callback(CategoryEditStatus.CATEGORY_ALREADY_EXIST)
        } else {
            viewModelScope.launch {
                categoryRepository.addCategory(
                    category = category,
                    callback = { result ->
                        if (result) {
                            callback(CategoryEditStatus.SUCCESS)
                        } else {
                            callback(CategoryEditStatus.FAILED)
                        }
                    }
                )
            }
        }
    }

    fun updateCategory(category: Category, callback: (CategoryEditStatus) -> Unit = {}) {
        //@TODO すでに存在するかチェックは関数化したほうが良いかも
        val isNameAlreadyExists = allCategories.value.any { it.name == category.name }
        if (isNameAlreadyExists) {
            callback(CategoryEditStatus.CATEGORY_ALREADY_EXIST)
        } else {
            viewModelScope.launch {
                categoryRepository.updateCategory(
                    category = category,
                    callback = { result ->
                        if (result) {
                            callback(CategoryEditStatus.SUCCESS)
                        } else {
                            callback(CategoryEditStatus.FAILED)
                        }
                    }
                )
            }
        }
    }

    fun removeCategory(category: Category, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            categoryRepository.removeCategory(
                category,
                callback = { result ->
                    if (result) {
                        callback(true)
                    } else {
                        callback(false)
                    }

                }
            )
        }
    }
}