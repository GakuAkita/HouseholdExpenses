package gaku.original.myapplication.ui.screens.global.expenseAddEdit

import android.os.Message
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import timber.log.Timber

data class ExpenseAddEditUiState(
    val amount:Long? = null,
    val message: String? = null,
    val isLoading: Boolean = false
)

class ExpenseAddEditViewModel(
    private val expenseRepository: ExpenseRepository,
    private val appTimeZoneRepository: AppTimeZoneRepository
    //private val categoryRepository:
): ViewModel() {

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                val expenseRepository = app.appContainer.sessionContainer!!.expenseRepository
                val appTimeZoneRepository = app.appContainer.sessionContainer!!.appTimeZoneRepository
                ExpenseAddEditViewModel(expenseRepository, appTimeZoneRepository)
            }
        }
    }

    init{
        Timber.d("Created. ${hashCode()}")
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}

//@HiltViewModel
//class ExpenseAddEditViewModel @Inject constructor(
//    private val expenseSharedViewModel: ExpenseSharedViewModel,
//    private val tmpExpenseViewModel: TemporaryExpenseViewModel,
//    private val categoryAssignmentUseCase: CategoryAssignmentUseCase,
//) : ViewModel() {

//    override fun onCleared() {
//        super.onCleared()
//        LogAkitaDebug("ExpenseAddEditViewModel cleared")
//    }
//
//    private val _initialExpenseList = tmpExpenseViewModel.tmpExpenseList
//
//    /* 初期値だけTempExpenseから受け取ってあとはこっちで保持 */
//    private val _expenseList = MutableStateFlow(
//        _initialExpenseList
//    )
//    val expenseList: StateFlow<List<Expense>> = _expenseList
//
//    //これリアルタイム同期するのか？ 他端末からCategoryを追加してみて、反映されるかみてみる
//    val allCategories: StateFlow<List<Category>> get() = expenseSharedViewModel.allCategories
//
//    private val _splitInputEnabled = MutableStateFlow(false)
//    val splitInputEnabled: StateFlow<Boolean> = _splitInputEnabled
//
//    /* 分割入力のときの合計金額 */
//    private val _totalAmount = MutableStateFlow<Long?>(0L)
//    val totalAmount: StateFlow<Long?> = _totalAmount
//    private var _initTotalAmount = false/* こいつはview側で見る必要はない */
//
//    private val _loadingState = MutableStateFlow(false)
//    val loadingState: StateFlow<Boolean> = _loadingState
//
//    /* 分割入力で選択したindexを覚えておくだけ */
//    private var selectedIndex: Int? = null
//    fun setSelectedIndex(index: Int) {
//        selectedIndex = index
//    }
//
//    fun updateTotalAmount(amount: Long?) {
//        _totalAmount.value = amount
//    }
//
//    fun switchSplitInput() {
//        /* ここで合計金額を転写しておく */
//        if (!_initTotalAmount) {
//            _initTotalAmount = true
//            /* 転写するのは一度だけ。その後はViewで編集 */
//            /* まあ入力制限しているのでamountがnullのときはないのだが、、 */
//            _totalAmount.value = getHeadExpense().amount ?: 0L
//
//            /* 先頭費用は0にしておいた方が良い */
//            updateExpenseAmountAt(index = 0, 0L)
//        }
//        _splitInputEnabled.value = !_splitInputEnabled.value
//
//        /**
//         * スイッチしたあと、ONにしたときはExpenseを自動で加えてもいいかもな。
//         */
//        if (_splitInputEnabled.value) {
//            /* 分割代入をONにした場合、Expenseを足しておく。 */
//            addExpenseToList()
//            calcLastExpenseAmount()
//        } else {
//            /**
//             *  ボタンをオフに戻した時
//             *  リストの先頭以外を消す
//             * */
//            removeExpenseExceptHead()
//            /* totalAmountを先頭にコピーしたほうがいいか */
//            updateExpenseAmountAt(0, _totalAmount.value)
//        }
//    }
//
//    fun setLoadingState(state: Boolean) {
//        _loadingState.value = state
//    }
//
//    /* 設定のタイムゾーンに合わせて現在日付 */
//    fun getTimeZoneDate(): LocalDate {
//        /* とりあえず日本で固定 */
//        AppTimeZone.isoStringToLocalDateTime(getHeadExpense().datetime)?.let {
//            return it.toLocalDate()
//        }
//        return AppTimeZone.getCurrentTimeInZone().toLocalDate()
//    }
//
//    /* 設定のタイムゾーンに合わせた現在時間 */
//    fun getTimeZoneTime(): LocalTime {
//        AppTimeZone.isoStringToLocalDateTime(getHeadExpense().datetime)?.let {
//            return it.toLocalTime()
//        }
//        return AppTimeZone.getCurrentTimeInZone().toLocalTime()
//    }
//
//    fun getSeparatedGeneratedType(): List<String> {
//        val buf = getHeadExpense().generatedType
//            ?: /* ここに来ることはない */
//            return emptyList()
//        return separateStringByBars(buf)
//    }
//
//    fun getGeneratedTypeDisplay(): String {
//        val buf = getHeadExpense().generatedType
//            ?: /* ここに来ることはない */
//            return "エラー"
//
//        val (mainType, subType) = convertGeneratedTypeToDisplayName(buf)
//        if (subType == null) {
//            return mainType
//        } else {
//            return "${mainType}/${subType}"
//        }
//    }
//
//    fun getHeadExpense(): Expense {
//        return expenseList.value.first()
//    }
//
//    fun addExpenseToList(newExpense: Expense = getDefaultExpense()) {
//        _expenseList.value += newExpense
//    }
//
//    fun updateExpense(newExpense: Expense) {
//        updateExpenseAt(0, newExpense)
//    }
//
//    fun updateExpenseAt(index: Int, newExpense: Expense) {
//        _expenseList.value = _expenseList.value.toMutableList().apply {
//            if (index in indices) {
//                this[index] = newExpense
//            }
//        }
//    }
//
//    fun removeExpenseExceptHead() {
//        _expenseList.value = _expenseList.value.take(1)
//    }
//
//    fun removeExpenseAt(index: Int) {
//        if (_expenseList.value.size > 1) {
//            _expenseList.value = _expenseList.value.toMutableList().apply {
//                if (index in indices) {
//                    removeAt(index)
//                }
//            }
//        }
//    }
//
//    fun updateExpenseDatetime(datetimeStr: String) {
//        /* 先頭のdatetimeを更新して保存するときに全部コピーするので更新するのは先頭だけで良い */
//        updateExpense(
//            getHeadExpense().copy(datetime = datetimeStr)
//        )
//    }
//
//    fun calcLastExpenseAmount() {
//        val arr = _expenseList.value
//        val size = arr.size
//        if (size > 1) {
//            val sumBeforeLast = arr.dropLast(1).sumOf { it.amount ?: 0L }
//            val remaining = (_totalAmount.value ?: 0L) - sumBeforeLast
//            updateExpenseAt(
//                size - 1,
//                arr[size - 1].copy(
//                    amount = remaining
//                )
//            )
//        }
//    }
//
//    fun calcExpenseListSum(): Long {
//        val arr = expenseList.value
//        val sum = arr.sumOf { it.amount ?: 0L }
//        return sum
//    }
//
//    /* selectedIndexを使って更新 */
//    fun updateExpenseAmountAtSelectedIndex(newAmount: Long?) {
//        val index = selectedIndex ?: return
//        updateExpenseAmountAt(index, newAmount)
//    }
//
//    // 各項目を個別に更新するメソッド
//    fun updateExpenseAmountAt(index: Int = 0, newAmount: Long?) {
//        updateExpenseAt(
//            index,
//            _expenseList.value[index].copy(
//                amount = newAmount
//            )
//        )
//        /**
//         * expenseListのサイズが1以上のとき、最後の要素は自動計算
//         */
//        calcLastExpenseAmount()
//    }
//
//    fun updateExpenseCategoryAt(index: Int = 0, newCategory: Category?) {
//        updateExpenseAt(
//            index,
//            _expenseList.value[index].copy(
//                category = newCategory
//            )
//        )
//    }
//
//    fun updateExpenseNoteAt(index: Int = 0, newNote: String) {
//        updateExpenseAt(
//            index,
//            _expenseList.value[index].copy(
//                note = newNote
//            )
//        )
//    }
//
//    fun updateExpenseItemNameAt(index: Int = 0, itemName: String) {
//        updateExpenseAt(
//            index,
//            _expenseList.value[index].copy(
//                itemName = itemName
//            )
//        )
//    }
//
//    /**
//     * こいつらは共通なのでheadだけ変更して、追加するときに
//     * すべて代入する
//     */
//    fun updateExpenseStoreName(storeName: String) {
//        updateExpense(
//            getHeadExpense().copy(
//                storeName = storeName
//            )
//        )
//    }
//
//    fun resetExpenseList() {
//        _expenseList.value = _initialExpenseList
//    }
//
//    fun copyCommonPropertyToList() {
//        val head = getHeadExpense()
//        val updatedList = _expenseList.value.mapIndexed { index, expense ->
//            if (index == 0) {
//                expense // 先頭はそのまま
//            } else {
//                expense.copy(
//                    datetime = head.datetime,
//                    generatedType = head.generatedType,
//                    storeName = head.storeName
//                )
//            }
//        }
//        _expenseList.value = updatedList
//    }
//
//    fun addExpenseToDb(callback: (FuncStatusInfo) -> Unit) {
//
//        setLoadingState(true)
//        /**
//         * ここで中身チェックを行った方が良い。
//         * あくまでViewではこいつを呼び出すだけで。
//         */
//        copyCommonPropertyToList()
//        var cnt = 0
//        viewModelScope.launch {
//            for (ex in _expenseList.value) {
//                val ret = expenseSharedViewModel.addExpense(
//                    ex
//                )
//                if (ret is FuncResultWithData.Success) {
//                    cnt++
//                }
//            }
//
//            if (_expenseList.value.size == cnt) {
//                callback(
//                    FuncStatusInfo(
//                        FuncStatus.SUCCESS,
//                        "追加に成功しました"
//                    )
//                )
//            } else {
//                setLoadingState(false)
//                callback(
//                    FuncStatusInfo(
//                        status = FuncStatus.FAILED, errorMessage = "追加に失敗しました"
//                    )
//                )
//            }
//        }
//    }
//
//    fun updateExpenseToDb(onStart: () -> Unit, callback: (FuncStatusInfo) -> Unit) {
//        setLoadingState(true)
//        onStart()
//        var cnt: Int = 0
//        copyCommonPropertyToList()
//        viewModelScope.launch {
//            for (ex in _expenseList.value) {
//                if (ex.id == null) {
//                    /* 新規作成 */
//                    val addRet = expenseSharedViewModel.addExpense(ex)
//                    if (addRet is FuncResultWithData.Success) {
//                        cnt++
//                    }
//                } else {
//                    /* update */
//                    val updateRet = expenseSharedViewModel.updateExpense(ex)
//                    if (updateRet.status == FuncStatus.SUCCESS) {
//                        cnt++
//                    }
//                }
//            }
//            if (cnt == _expenseList.value.size) {
//                callback(
//                    FuncStatusInfo(
//                        FuncStatus.SUCCESS,
//                        "更新に成功しました"
//                    )
//                )
//            } else {
//                callback(
//                    FuncStatusInfo(
//                        status = FuncStatus.FAILED, errorMessage = "更新に失敗しました"
//                    )
//                )
//            }
//        }
//    }
//
//    fun removeExpenseToDb(onStart: () -> Unit, callback: (FuncStatusInfo) -> Unit) {
//        /**
//         * 分割入力のときは、
//         * それをオフにしてからにする
//         */
//        onStart()
//        viewModelScope.launch {
//            val ret = expenseSharedViewModel.removeExpense(getHeadExpense())
//            callback(ret)
//        }
//    }
//
//    /* カテゴリーを更新する。通信エラーが起きているとカテゴリーが取れていないときがある */
//    fun updateStoredCategories(
//        callback: (FuncStatusInfo) -> Unit
//    ) {
//        expenseSharedViewModel.clearAllCategories()
//        viewModelScope.launch {
//            val ret = expenseSharedViewModel.fetchAllCategories()
//            val listenerRet = expenseSharedViewModel.addCategoryListeners()
//            callback(ret)
//        }
//    }
//
//    /* ------------------カテゴリー割当を扱う----------------------- */
//    fun addCategoryAssignment(
//        onStart: () -> Unit = {},
//        assignment: CategoryAssignment,
//        namePattern: CategoryAssignNamePattern,
//        callback: (FuncStatusInfo) -> Unit = {}
//    ) {
//        onStart()
//        viewModelScope.launch {
////            val ret =
////                categoryAssignmentUseCase.addCategoryAssignmentWithCheck(assignment, namePattern)
////            callback(ret.toFuncStatusInfo())
//        }
//    }
//
//    /*******************************/
//    /* 費用を追加/削除する */
//    /*******************************/
//    fun removeExpenseFromListAtSelectedIndex() {
//        val index = selectedIndex ?: return
//        removeExpenseFromListAt(index)
//    }
//
//    fun removeExpenseFromListAt(index: Int) {
//        removeExpenseAt(index)
//        calcLastExpenseAmount()
//    }
//
//    fun getExpenseAmountAtSelectedIndex(): Long {
//        val index = selectedIndex ?: return 0L
//        return _expenseList.value[index].amount ?: 0L
//    }
//}