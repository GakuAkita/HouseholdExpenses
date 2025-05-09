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
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.ExpenseRepository
import gaku.original.myapplication.data.InitialCategories
import gaku.original.myapplication.data.generatedType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExpenseSharedViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val dbListenerManager: DbListenerManager,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

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

    //サインインしたタイミングで実行する。
    suspend fun addExpenseCategoryChildEventListener(callback: (SuspendFuncStatus) -> Unit = {}): SuspendFuncStatus {
        //実行されたタイミングのtimeだけあればよい。
        val firstFetchedTime = System.currentTimeMillis()
        var ret = SuspendFuncStatus.FAILED

        val expenseRef = getExpenseRef {
            callback(it)
        }

        if (expenseRef == null) {
            return ret
        }

        val categoryRef = getCategoryRef {
            callback(it)
        }

        if (categoryRef == null) {
            return ret
        }

        val queryForAddedExpense =
            expenseRef.orderByChild("timestamp").startAt(firstFetchedTime.toDouble())

        val queryForAddedCategory =
            categoryRef.orderByChild("timestamp").startAt(firstFetchedTime.toDouble())

        //Expenseのリスナー
        dbListenerManager.addListener(
            queryForAddedExpense,
            expenseListAddChildEventListener
        )
        dbListenerManager.addListener(expenseRef, expenseListWatchChildEventListener)

        //Categoryのリスナー
        dbListenerManager.addListener(queryForAddedCategory, categoryListAddChildEventListener)
        dbListenerManager.addListener(getExpenseRef(), categoryListWatchChildEventListener)
        //リスナーが溜まっているかどうかは、UIに表示してみればよいか。
        /* addListenerが非同期的にうまく行っているか確認する方法はないっぽい。callbackとかない。 */
        ret = SuspendFuncStatus.SUCCESS
        return ret
    }

    fun clearExpenseChildEventListener() {
        dbListenerManager.removeAllListeners()
    }

    /*******************Expense CRUD関連**************************/
    init {
        Log.d("ExpenseSharedViewModel", "Init was called.")

        /* これがないと、MainViewに遷移したときに_allExpensesに値が入らない */
        //@TODO onSignedInという関数を作り、それに初期化処理をいれる
        if (firebaseAuth.currentUser != null &&
            _allExpenses.value.isEmpty()
        ) {
            viewModelScope.launch {
                val fetchStatus = fetchAllExpenses(
                    onStart = {
                        _expensesLoadingStatus.value = LoadingStatus.LOADING
                    },
                    callback = { status ->
                        if (status == SuspendFuncStatus.SUCCESS) {
                            _expensesLoadingStatus.value = LoadingStatus.COMPLETED
                        } else if (status == SuspendFuncStatus.TIMEOUT) {
                            _expensesLoadingStatus.value = LoadingStatus.TIMEOUT
                        } else {
                            _expensesLoadingStatus.value = LoadingStatus.ERROR
                        }
                    })

                if (fetchStatus == SuspendFuncStatus.SUCCESS) {
                    val listenerAddStatus = addExpenseCategoryChildEventListener()
                }
            }
        } else {
            Log.d("ExpenseSharedViewModel", "User is not signed in.")
        }

        if (firebaseAuth.currentUser != null &&
            _allCategories.value.isEmpty()
        ) {
            viewModelScope.launch {
                fetchAllCategories()
            }
        }
    }

    /*@TODO 途中で止まったときに、どうやって復旧させるかとか考えないとな。*/
    suspend fun addUserInitialData(email: String, callback: (SuspendFuncStatus) -> Unit) {
        //呼び出すだけ。関数名が全く同じなので変えたほうが良いかも
        expenseRepository.addUserInitialData(email, callback)

        // デフォルトカテゴリーを並列で追加
        InitialCategories.categories.forEach { initialCategory ->
            //こっちは失敗してもいいから、このままでいいや。
            categoryRepository.addCategory(initialCategory, {})
        }
    }

    suspend fun fetchAllExpenses(
        onStart: () -> Unit = {},
        callback: (SuspendFuncStatus) -> Unit = {}
    ): SuspendFuncStatus {
        onStart()

        return withContext(Dispatchers.IO) {
            var ret: SuspendFuncStatus = SuspendFuncStatus.FAILED
            _allExpenses.value = expenseRepository.fetchAllExpenses { status ->
                ret = status
                callback(status)
            }
            Log.d("ExpenseSharedViewModel", "Expenses:${_allExpenses.value}")
            ret//こいつを返す
        }
    }

    suspend fun addExpense(
        expense: Expense,
        callback: (SuspendFuncStatus) -> Unit = {}
    ): SuspendFuncStatus {
        if (expense.generatedType == null) {
            expense.generatedType = generatedType.MANUAL
        }

        if (expense.note == null) {
            expense.note = ""
        }
        return expenseRepository.addExpense(expense, callback)
    }

    suspend fun updateExpense(
        expense: Expense,
        callback: (SuspendFuncStatus) -> Unit = {}
    ): SuspendFuncStatus {

        return expenseRepository.updateExpense(expense, callback)

    }

    suspend fun removeExpense(
        expense: Expense,
        callback: (SuspendFuncStatus) -> Unit = {}
    ): SuspendFuncStatus {
        return expenseRepository.removeExpense(expense, callback)
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


    suspend fun fetchAllCategories(callback: (SuspendFuncStatus) -> Unit = {}): SuspendFuncStatus {
        var status: SuspendFuncStatus = SuspendFuncStatus.FAILED

        val categories = categoryRepository.fetchAllCategories {
            status = it
            callback(it)
        }

        _allCategories.value = categories
        Log.d("CategoryViewModel", "Categories:${_allCategories.value}")
        return status
    }

    /*
    被りチェックはここで入れたほうが良いな。
    CategoryEditView用の関数を作りそこで被りチェックを入れても良いが、そうすると、
    SharedViewModelを他のviewModelでチェックしてCategoryを追加するときに同じ機能を実装することになる。
    */
    suspend fun addCategory(
        category: Category,
        onDuplicateCategory: () -> Unit = {},
        callback: (SuspendFuncStatus) -> Unit = {}
    ): SuspendFuncStatus {
        //@TODO オフラインのときの対応。categoriesがうまく取得できなかった時
        val isNameAlreadyExists = allCategories.value.any { it.name == category.name }
        if (isNameAlreadyExists) {
            onDuplicateCategory()
            return SuspendFuncStatus.FAILED
        } else {
            return categoryRepository.addCategory(
                category = category,
                callback = callback
            )
        }
    }

    suspend fun updateCategory(
        category: Category,
        onDuplicateCategory: () -> Unit = {},
        callback: (SuspendFuncStatus) -> Unit = {}
    ): SuspendFuncStatus {
        //@TODO すでに存在するかチェックは関数化したほうが良いかも
        val isNameAlreadyExists = allCategories.value.any { it.name == category.name }
        if (isNameAlreadyExists) {
            onDuplicateCategory()
            return SuspendFuncStatus.FAILED
        } else {
            return categoryRepository.updateCategory(
                category = category,
                callback = callback
            )

        }
    }

    suspend fun removeCategory(
        category: Category,
        callback: (SuspendFuncStatus) -> Unit = {}
    ): SuspendFuncStatus {
        return categoryRepository.removeCategory(
            category,
            callback = callback
        )
    }
}