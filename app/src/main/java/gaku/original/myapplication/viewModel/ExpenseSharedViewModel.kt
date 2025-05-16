package gaku.original.myapplication.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gaku.original.myapplication.FirestoreListenerManager
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
import java.time.YearMonth
import javax.inject.Inject

class ExpenseSharedViewModel @Inject constructor(
    private val expenseRepository: ExpenseFirestoreRepository,
    private val categoryRepository: CategoryFirestoreRepository,
    private val listenerManager: FirestoreListenerManager
) : ViewModel() {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    //@TODO 総データ量が多くないので、データをすべて引っ張ってくる仕様だが、将来的には数ヶ月分だけとってくる形にする
    private val _storedExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val allExpenses: StateFlow<List<Expense>> get() = _storedExpenses

    fun clearAllExpenses() {
        _storedExpenses.value = emptyList()
    }

    //emptyList()のとき、Loadingがうまく行ってないのか、シンプルに何も保存されていないのかの区別がつかない
    private val _expensesLoadingStatus = MutableStateFlow(LoadingStatus.COMPLETED)
    val expensesLoadingStatus: StateFlow<LoadingStatus> get() = _expensesLoadingStatus

    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
    val allCategories: StateFlow<List<Category>> get() = _allCategories
    fun clearAllCategories() {
        _allCategories.value = emptyList()
    }

    /* Expense関連はここにまとめておく */
    fun addExpensesListeners(yearMonth: YearMonth) {
        addExpenseListenerModifiedRemoved(yearMonth)
        addExpenseListenerAdded(yearMonth)
    }

    fun addExpenseListenerModifiedRemoved(yearMonth: YearMonth = YearMonth.now()) {
        listenerManager.listenToExpensesModifiedRemoved(
            yearMonth = yearMonth,
            onModified = {
                /* _allExpensesを更新する */
                _storedExpenses.value = _storedExpenses.value.map { expense ->
                    if (expense.id == it.id) {
                        it
                    } else {
                        expense
                    }
                }
            },
            onRemoved = {
                _storedExpenses.value = _storedExpenses.value.filter { expense ->
                    expense.id != it.id
                }
            })
    }

    fun addExpenseListenerAdded(yearMonth: YearMonth = YearMonth.now()) {
        listenerManager.listenToNewExpensesOnly(
            onAdded = {
                /* _allExpensesに追加する */
                /* 取得月の範囲内に入っているかここでフィルターしてもいいな。 */
                /* まあ、どうせListViewでフィルターかけるからいいか。 */
                _storedExpenses.value += it
            })
    }

    fun clearAllListeners() {
        listenerManager.clearAllListeners()
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
        clearAllListeners()
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

                        /* fetchしたあとにリスナーを追加しないとだめだわ。 */
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
            /* 他にやることがあるのであればここへ、、 */

            /* 最後にフラグを下げる。@TODO 途中停止なのか判定できたほうがいいか、、、 */
            _initFetchedDone.value = true
        }
    }

    fun onSignedOut() {
        clearPossession()
        setInitFetchedDone(false)
    }

    suspend fun fetchMonthsExpenses(
        fromMonth: YearMonth,
        toMonth: YearMonth,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): SuspendFuncStatusInfo {
        val fetchResult = expenseRepository.fetchMonthsExpenses(
            fromMonth,
            toMonth,
            callback = callback
        )
        val statusInfo = fetchResult.toSuspendFuncStatusInfo()

        if (statusInfo.status == SuspendFuncStatus.SUCCESS) {
            _storedExpenses.value = fetchResult.data ?: emptyList()
        }
        Log.d(className, "Expenses:${_storedExpenses.value}")
        return statusInfo
    }

    /**
     * これは使わない
     */
    suspend fun fetchAllExpenses(
        onStart: () -> Unit = {},
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): SuspendFuncStatusInfo {
        onStart()

        val fetchStatusInfo = expenseRepository.fetchAllExpenses(callback = callback)

        val statusInfo = fetchStatusInfo.toSuspendFuncStatusInfo()

        /* 成功したときだけ書き換える。 */
        if (statusInfo.status == SuspendFuncStatus.SUCCESS) {
            _storedExpenses.value = fetchStatusInfo.data ?: emptyList()
        }
        Log.d(className, "Expenses:${_storedExpenses.value}")

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
        val isNameAlreadyExists = _allCategories.value.any { it.name == category.name }
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
        val isNameAlreadyExists = _allCategories.value.any { it.name == category.name }
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