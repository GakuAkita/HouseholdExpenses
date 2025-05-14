package gaku.original.myapplication.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.firestore.CollectionReference
import gaku.original.myapplication.ListenerManager
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.FirestoreRepository.CategoryFirestoreRepository
import gaku.original.myapplication.data.FirestoreRepository.ExpenseFirestoreRepository
import gaku.original.myapplication.data.InitialCategories
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.generatedType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExpenseSharedViewModel @Inject constructor(
    private val expenseRepository: ExpenseFirestoreRepository,
    private val categoryRepository: CategoryFirestoreRepository,
    private val listenerManager: ListenerManager
) : ViewModel() {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    //@TODO 総データ量が多くないので、データをすべて引っ張ってくる仕様だが、将来的には数ヶ月分だけとってくる形にする
    private val _allExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val allExpenses: StateFlow<List<Expense>> get() = _allExpenses

    fun clearAllExpenses() {
        _allExpenses.value = emptyList()
    }

    //emptyList()のとき、Loadingがうまく行ってないのか、シンプルに何も保存されていないのかの区別がつかない
    private val _expensesLoadingStatus = MutableStateFlow(LoadingStatus.COMPLETED)
    val expensesLoadingStatus: StateFlow<LoadingStatus> get() = _expensesLoadingStatus

    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
    val allCategories: StateFlow<List<Category>> get() = _allCategories
    fun clearAllCategories() {
        _allCategories.value = emptyList()
    }

    //realtimeDbReferenceから取っても良いが、引数が増えるのでdbListenerManagerから取る
//    suspend fun getExpenseRef(callback: (SuspendFuncStatus) -> Unit = {}): DatabaseReference? {
//        return dbListenerManager.getExpenseRef(callback)
//    }

//    suspend fun getCategoryRef(callback: (SuspendFuncStatus) -> Unit = {}): DatabaseReference? {
//        return dbListenerManager.getCategoryRef(callback)
//    }

    fun getExpensesColRef(): CollectionReference? {
        return expenseRepository.getExpensesColRef()
    }

    fun getCategoriesRef(): CollectionReference? {
        return categoryRepository.getCategoriesColRef()
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
            Log.d(className, "onChildChanged(Expense) was called.")
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
                    Log.d(className, "Expense updated: ${updatedExpense.id}")
                }
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            Log.d(className, "onChildRemoved(Expense) was called.")
            val removedExpense = snapshot.getValue(Expense::class.java)
            removedExpense?.let {
                viewModelScope.launch {
                    _allExpenses.value = _allExpenses.value.filterNot { expense ->
                        expense.id == removedExpense.id
                    }
                    Log.d(className, "Expense removed: $removedExpense")
                }
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
    }

    //サインインしたタイミングで実行する。
    suspend fun addExpenseCategoryChildEventListener(callback: (SuspendFuncStatusInfo) -> Unit = {}): SuspendFuncStatusInfo {
        //実行されたタイミングのtimeだけあればよい。
        val firstFetchedTime = System.currentTimeMillis()
        var ret = SuspendFuncStatus.FAILED

        val expenseRef = getExpensesColRef()

        if (expenseRef == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Expensesコレクションが取得できませんでした"
            )
            callback(statusInfo)
            return statusInfo
        }

        val categoryRef = getCategoriesRef()

        if (categoryRef == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Categoriesコレクションが取得できませんでした"
            )
            callback(statusInfo)
            return statusInfo
        }

        return SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, "デバッグ中、、、")
//        val queryForAddedExpense =
//            expenseRef.orderByChild("timestamp").startAt(firstFetchedTime.toDouble())
//
//        val queryForAddedCategory =
//            categoryRef.orderByChild("timestamp").startAt(firstFetchedTime.toDouble())
//
//        //Expenseのリスナー
//        dbListenerManager.addListener(
//            queryForAddedExpense,
//            expenseListAddChildEventListener
//        )
//        dbListenerManager.addListener(expenseRef, expenseListWatchChildEventListener)
//
//        //Categoryのリスナー
//        dbListenerManager.addListener(queryForAddedCategory, categoryListAddChildEventListener)
//        dbListenerManager.addListener(categoryRef, categoryListWatchChildEventListener)
//        //リスナーが溜まっているかどうかは、UIに表示してみればよいか。
//        /* addListenerが非同期的にうまく行っているか確認する方法はないっぽい。callbackとかない。 */
//        ret = SuspendFuncStatus.SUCCESS
//        return ret
    }

    fun clearExpenseChildEventListener() {
        listenerManager.removeAllListeners()
    }

    /*******************Expense CRUD関連**************************/
    init {
        Log.d(className, "Init was called.")
    }


    /*@TODO 途中で止まったときに、どうやって復旧させるかとか考えないとな。*/
    suspend fun addUserInitialData(
        email: String,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        //呼び出すだけ。関数名が全く同じなので変えたほうが良いかも
        val statusInfo = expenseRepository.addUserInitialData(email, callback)
        if (statusInfo.status != SuspendFuncStatus.SUCCESS) {
            return statusInfo
        }

        // デフォルトカテゴリーを並列で追加
        InitialCategories.categories.forEach { initialCategory ->
            //こっちは失敗してもいいから、このままでいいや。
            categoryRepository.addCategory(initialCategory, {})
        }

        /* カテゴリーはミスってもいいや */
        return statusInfo
    }

    /* これがtrueになっていれば、fetchAllExpensesを走らせる */
    private val _initFetchedDone = MutableStateFlow(false);
    val initFetchedDone: StateFlow<Boolean> get() = _initFetchedDone

    fun setInitFetchedDone(value: Boolean) {
        _initFetchedDone.value = value
    }

    /* このViewModel内で保持しているExpenses等をクリアにしたい。 */
    private fun clearPossession() {
        clearAllExpenses()
        clearAllCategories()
        clearExpenseChildEventListener()
    }

    suspend fun onSignedIn(callback: (SuspendFuncStatusInfo) -> Unit) {
        if (_initFetchedDone.value) {
            /* アプリを立ち上げて初回実行時に行う */
            Log.d(className, "すでに実行されている.....")
            return;
        }

        /* サインアウト時にほとんどクリアしているが、ここでも行っておく */
        clearPossession()

        viewModelScope.launch {
            Log.d(className, "init fetch is executed..")
            var ret = fetchAllExpenses(
                onStart = {
                    _expensesLoadingStatus.value = LoadingStatus.LOADING
                },
                callback = { statusInfo ->
                    if (statusInfo.status == SuspendFuncStatus.SUCCESS) {
                        _expensesLoadingStatus.value = LoadingStatus.COMPLETED
                    } else if (statusInfo.status == SuspendFuncStatus.TIMEOUT) {
                        _expensesLoadingStatus.value = LoadingStatus.TIMEOUT
                    } else {
                        _expensesLoadingStatus.value = LoadingStatus.ERROR
                    }
                })

            if (ret.status != SuspendFuncStatus.SUCCESS) {
                callback(ret)
                return@launch
            }

            ret = fetchAllCategories()
            if (ret.status != SuspendFuncStatus.SUCCESS) {
                callback(ret)
                return@launch
            }

            ret = addExpenseCategoryChildEventListener(callback)
            if (ret.status != SuspendFuncStatus.SUCCESS) {
                return@launch
            }

            /* 他にやることがあるのであればここへ、、 */

            /* 最後にフラグを下げる。@TODO 途中停止なのか判定できたほうがいいか、、、 */
            _initFetchedDone.value = true
        }
    }

    fun onSignedOut() {
        clearPossession()
        setInitFetchedDone(false)
    }


    suspend fun fetchAllExpenses(
        onStart: () -> Unit = {},
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): SuspendFuncStatusInfo {
        onStart()

        val fetchStatusInfo = expenseRepository.fetchAllExpenses(callback = callback)

        val statusInfo = fetchStatusInfo.toSuspendFuncStatusInfo()

        /* 成功したときだけ書き換える。 */
        if (statusInfo.status == SuspendFuncStatus.SUCCESS) {
            _allExpenses.value = fetchStatusInfo.data ?: emptyList()
        }
        Log.d(className, "Expenses:${_allExpenses.value}")

        return statusInfo
    }

    suspend fun addExpense(
        expense: Expense,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): SuspendFuncStatusInfo {
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
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): SuspendFuncStatusInfo {

        return expenseRepository.updateExpense(expense, callback)

    }

    suspend fun removeExpense(
        expense: Expense,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): SuspendFuncStatusInfo {
        return expenseRepository.removeExpense(expense, callback)
    }

    /*******************Category CRUD関連**************************/
    /* @TODO 将来的にはallExpensesと同じようなローカルに保持しておく。(カテゴリー自体は数が多くならないので今は毎回fetchする感じにする) */
    private val categoryListAddChildEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d(className, "onChildAdded(Category) was called.")
            val newCategory = snapshot.getValue(Category::class.java)
            newCategory?.let {
                viewModelScope.launch {
                    Log.d(
                        className,
                        "_allExpenses.value size: ${_allCategories.value.size}"
                    )
                    _allCategories.value += newCategory
                    Log.d(className, "Expense added: $newCategory")
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
            Log.d(className, "onChildChanged(Category) was called.")
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
                    Log.d(className, "Category updated: ${updatedCategory.id}")
                }
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            Log.d(className, "onChildRemoved(Category) was called.")
            val removedExpense = snapshot.getValue(Expense::class.java)
            removedExpense?.let {
                viewModelScope.launch {
                    _allCategories.value = _allCategories.value.filterNot { expense ->
                        expense.id == removedExpense.id
                    }
                    Log.d(className, "Category removed: $removedExpense")
                }
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
    }


    suspend fun fetchAllCategories(callback: (SuspendFuncStatusInfo) -> Unit = {}): SuspendFuncStatusInfo {

        val fetchResult = categoryRepository.fetchAllCategories(callback = callback)

        if (fetchResult.status == SuspendFuncStatus.SUCCESS) {
            _allCategories.value = fetchResult.data ?: emptyList()
        }
        Log.d("ExpenseSharedViewModel", "Categories:${_allCategories.value}")
        return fetchResult.toSuspendFuncStatusInfo()
    }

    /*
    被りチェックはここで入れたほうが良いな。
    CategoryEditView用の関数を作りそこで被りチェックを入れても良いが、そうすると、
    SharedViewModelを他のviewModelでチェックしてCategoryを追加するときに同じ機能を実装することになる。
    */
    suspend fun addCategory(
        category: Category,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): SuspendFuncStatusInfo {
        //@TODO オフラインのときの対応。categoriesがうまく取得できなかった時
        val isNameAlreadyExists = allCategories.value.any { it.name == category.name }
        if (isNameAlreadyExists) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "${category.name} はすでに存在しています。"
            )
            callback(statusInfo)
            return statusInfo
        } else {
            return categoryRepository.addCategory(
                category = category,
                callback = callback
            )
        }
    }

    suspend fun updateCategory(
        category: Category,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): SuspendFuncStatusInfo {
        //@TODO すでに存在するかチェックは関数化したほうが良いかも
        val isNameAlreadyExists = allCategories.value.any { it.name == category.name }
        if (isNameAlreadyExists) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "${category.name}はすでに存在しています。"
            )
            callback(statusInfo)
            return statusInfo
        } else {
            return categoryRepository.updateCategory(
                category = category,
                callback = callback
            )
        }
    }

    suspend fun removeCategory(
        category: Category,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): SuspendFuncStatusInfo {
        return categoryRepository.removeCategory(
            category,
            callback = callback
        )
    }
}