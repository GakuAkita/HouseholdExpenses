package gaku.original.myapplication.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gaku.original.myapplication.FirestoreListenerManager
import gaku.original.myapplication.data.Constants.MONTH_RANGE
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.Constants.TimeZoneOption
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.GeneratedType
import gaku.original.myapplication.data.dataClass.InitialCategories
import gaku.original.myapplication.repository.FirestoreRepository.ExpenseFirestoreRepository
import gaku.original.myapplication.repository.FirestoreRepository.UserSettingsFirestoreRepository
import gaku.original.myapplication.useCase.CategoryUseCase
import gaku.original.myapplication.utility.AppTimeZone
import gaku.original.myapplication.utility.LogAkitaDebug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

class ExpenseSharedViewModel @Inject constructor(
    private val expenseRepository: ExpenseFirestoreRepository,
    private val categoryUseCase: CategoryUseCase,
    private val userSettingsRepository: UserSettingsFirestoreRepository,
    private val listenerManager: FirestoreListenerManager
) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        Log.d(className, "${className}Cleared!!!!")
        clearPossession()
    }

    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    /**
     * また大規模にリファクタリングしたほうがいいかもな、
     * ViewModelがでかくなってきた。
     */

    //@TODO 総データ量が多くないので、データをすべて引っ張ってくる仕様だが、将来的には数ヶ月分だけとってくる形にする
    private val _storedExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val storedExpenses: StateFlow<List<Expense>> get() = _storedExpenses

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
    fun addAllListeners(yearMonth: YearMonth = AppTimeZone.getCurrentUtcYearMonth()) {
        addExpenseListenerModifiedRemoved(yearMonth)
        addExpenseListenerAdded()
        addCategoryListenerModifiedRemoved()
        addCategoryListenerAdded()
    }

    fun addExpenseListenerModifiedRemoved(yearMonth: YearMonth = AppTimeZone.getCurrentUtcYearMonth()) {
        listenerManager.listenToExpensesModifiedRemoved(
            yearMonth = yearMonth,
            monthNum = MONTH_RANGE,
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

    fun addExpenseListenerAdded(/*yearMonth: YearMonth = AppTimeZone.getCurrentUtcYearMonth()*/) {
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

    /*  */
    private val _isFirstSignIn = MutableStateFlow(true)
    val isFirstSignIn: StateFlow<Boolean> get() = _isFirstSignIn

    fun setIsFirstSignIn(value: Boolean) {
        _isFirstSignIn.value = value
    }

    /* このViewModel内で保持しているExpenses等をクリアにしたい。 */
    fun clearPossession() {
        Log.d(className, "clearPossession was called.")
        clearAllExpenses()
        clearAllCategories()
        clearAllListeners()
    }

    fun addInitialCategories(callback: (FuncStatusInfo) -> Unit) {
        viewModelScope.launch {
            for (category in InitialCategories.categories) {
                categoryUseCase.addCategory(category)
            }
        }
    }

    fun onSignedUp(callback: (FuncStatusInfo) -> Unit) {
        setIsFirstSignIn(false)//これでonSignInを走らせないようにする

        addAllListeners()
    }

    fun onSignedIn(callback: (FuncStatusInfo) -> Unit) {
        /* サインアウト時にほとんどクリアしているが、ここでも行っておく */
        clearPossession()
        _expensesLoadingStatus.value = LoadingStatus.LOADING
        /**
         * このfetchもどっちでやるか要件等だな、
         */
        viewModelScope.launch {
            Log.d(className, "init fetch is executed..")

            /**
             * ここの時間はUTC!!
             * なぜならfetchMonthsExpensesInternalでは
             * datetime(UTCで保存)に対してクエリしているから。
             */
            val utcYearMonth = AppTimeZone.getCurrentUtcYearMonth()
            var ret = fetchMonthsExpensesInternal(
                fromMonth = utcYearMonth.minusMonths(3),
                toMonth = utcYearMonth.plusMonths(3),
            )
            if (ret.status == FuncStatus.SUCCESS) {
                _expensesLoadingStatus.value = LoadingStatus.COMPLETED
                /* fetchしたあとにリスナーを追加しないとだめだわ。 */
            } else if (ret.status == FuncStatus.TIMEOUT) {
                _expensesLoadingStatus.value = LoadingStatus.TIMEOUT
            } else {
                _expensesLoadingStatus.value = LoadingStatus.ERROR
            }

            if (ret.status != FuncStatus.SUCCESS) {
                addAllListeners()
                callback(ret)
                return@launch
            }

            /* ここ失敗したらなんか状態管理しておいたほうがいいな。 */
            ret = fetchAllCategories()
            if (ret.status != FuncStatus.SUCCESS) {
                LogAkitaDebug("Unable to get categories!!")
                callback(ret)
                return@launch
            }
            addAllListeners()

            val tzRet = userSettingsRepository.getUserTimeZone()
            if (tzRet !is FuncResultWithData.Success) {
                LogAkitaDebug("Unable to get timezone!!")
            } else {
                Log.d(className, "userTimeZone:${AppTimeZone.currentZoneId.id}")
            }

            /* 最後だから */
            callback(tzRet.toFuncStatusInfo())
            /* 他にやることがあるのであればここへ、、 */
        }
    }

    fun onSignedOut() {
        clearPossession()
        setIsFirstSignIn(true)//
        /* サインアップした時用に、 タイムゾーンを日本にしておく*/
        AppTimeZone.updateStrZoneId(TimeZoneOption.JAPAN.id)
    }

    /**
     * fetch関連はこのViewModel内で行う！
     */
    fun fetchMonthsExpenses(
        fromMonth: YearMonth,
        toMonth: YearMonth,
        callback: (FuncStatusInfo) -> Unit
    ) {
        _expensesLoadingStatus.value = LoadingStatus.LOADING
        viewModelScope.launch {
            val statusInfo = fetchMonthsExpensesInternal(fromMonth, toMonth)
            when (statusInfo.status) {
                FuncStatus.SUCCESS -> {
                    _expensesLoadingStatus.value = LoadingStatus.COMPLETED
                }

                FuncStatus.TIMEOUT -> {
                    _expensesLoadingStatus.value = LoadingStatus.TIMEOUT
                }

                FuncStatus.FAILED -> {
                    _expensesLoadingStatus.value = LoadingStatus.ERROR
                }

                FuncStatus.WARNING -> {

                }
            }
            callback(statusInfo)
        }
    }

    private suspend fun fetchMonthsExpensesInternal(
        fromMonth: YearMonth,
        toMonth: YearMonth,
    ): FuncStatusInfo {
        val fetchResult = expenseRepository.fetchMonthsExpenses(
            fromMonth,
            toMonth,
        )
        if (fetchResult is FuncResultWithData.Success) {
            //成功のときだけ更新
            _storedExpenses.value = fetchResult.data
            Log.d(className, "Expenses:${_storedExpenses.value}")
        }
        return fetchResult.toFuncStatusInfo()
    }

    /**
     * これは使わない
     */
//    private suspend fun fetchAllExpensesInternal(
//        onStart: () -> Unit = {},
//        callback: (FuncStatusInfo) -> Unit = {}
//    ): FuncStatusInfo {
//        onStart()
//        val fetchResult = expenseRepository.fetchAllExpenses(callback = callback)
//        if (fetchResult !is FuncResultWithData.Success) {
//            return fetchResult.toFuncStatusInfo()
//        }
//        val statusInfo = fetchStatusInf.toFuncStatusInfo()
//
//        /* 成功したときだけ書き換える。 */
//        if (statusInfo.status == FuncStatus.SUCCESS) {
//            _storedExpenses.value = fetchStatusInfo.data ?: emptyList()
//        }
//        Log.d(className, "Expenses:${_storedExpenses.value}")
//
//        return statusInfo
//    }


    suspend fun addExpense(
        expense: Expense
    ): FuncResultWithData<Expense> {
        /* ここでMANUALにしている */
        if (expense.generatedType == null) {
            expense.generatedType = GeneratedType.MANUAL
        }

        if (expense.note == null) {
            expense.note = ""
        }
        return expenseRepository.addExpense(expense)
    }

    suspend fun updateExpense(
        expense: Expense
    ): FuncStatusInfo {

        return expenseRepository.updateExpense(expense)

    }

    suspend fun removeExpense(
        expense: Expense,
        callback: (FuncStatusInfo) -> Unit = {}
    ): FuncStatusInfo {
        return expenseRepository.removeExpense(expense)
    }

    /*******************Category CRUD関連**************************/
    suspend fun fetchAllCategories(): FuncStatusInfo {

        val fetchResult = categoryUseCase.fetchAllCategories()

        if (fetchResult is FuncResultWithData.Success) {
            _allCategories.value = fetchResult.data
        }
        Log.d(className, "Categories:${_allCategories.value}")
        return fetchResult.toFuncStatusInfo()
    }

    /*
    被りチェックはここで入れたほうが良いな。
    CategoryEditView用の関数を作りそこで被りチェックを入れても良いが、そうすると、
    SharedViewModelを他のviewModelでチェックしてCategoryを追加するときに同じ機能を実装することになる。
    */
    suspend fun addCategory(
        category: Category
    ): FuncResultWithData<Category> {
        //@TODO オフラインのときの対応。categoriesがうまく取得できなかった時
        val isNameAlreadyExists = _allCategories.value.any { it.name == category.name }
        if (isNameAlreadyExists) {
            val statusInfo = FuncResultWithData.Failure.GenericFailure(
                FuncStatus.FAILED,
                "${category.name} はすでに存在しています。"
            )
            return statusInfo
        } else {
            return categoryUseCase.addCategory(
                category = category,
            )
        }
    }

    suspend fun updateCategory(
        category: Category,
    ): FuncStatusInfo {
        //@TODO すでに存在するかチェックは関数化したほうが良いかも
        val isNameAlreadyExists = _allCategories.value.any { it.name == category.name }
        if (isNameAlreadyExists) {
            val statusInfo = FuncStatusInfo(
                FuncStatus.FAILED,
                "${category.name}はすでに存在しています。"
            )
            return statusInfo
        }

        return categoryUseCase.updateCategory(category)
    }

    suspend fun removeCategory(
        category: Category
    ): FuncStatusInfo {
        return categoryUseCase.removeCategory(category)
    }

    fun addCategoryListenerModifiedRemoved() {
        listenerManager.listenToCategoriesModifiedRemoved(
            onModified = {
                /* _allExpensesを更新する */
                _allCategories.value = _allCategories.value.map { category ->
                    if (category.id == it.id) {
                        it
                    } else {
                        category
                    }
                }
            },
            onRemoved = {
                _allCategories.value = _allCategories.value.filter { category ->
                    category.id != it.id
                }
            })
    }

    fun addCategoryListenerAdded() {
        listenerManager.listenToNewCategoriesOnly(
            onAdded = {
                _allCategories.value += it
            }
        )
    }

    fun addCategoryListeners() {
        addCategoryListenerAdded()
        addCategoryListenerModifiedRemoved()
    }

}