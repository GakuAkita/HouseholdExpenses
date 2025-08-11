package gaku.original.myapplication.viewModel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.Interface.CategoryAssignNamePattern
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.convertGeneratedTypeToDisplayName
import gaku.original.myapplication.useCase.CategoryAssignmentUseCase
import gaku.original.myapplication.utility.AppTimeZone
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.utility.separateStringByBars
import gaku.original.myapplication.viewModel.ExpenseSharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class ExpenseAddEditViewModel @Inject constructor(
    private val expenseSharedViewModel: ExpenseSharedViewModel,
    private val tmpExpenseViewModel: TemporaryExpenseViewModel,
    private val categoryAssignmentUseCase: CategoryAssignmentUseCase
) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        LogAkitaDebug("ExpenseAddEditViewModel cleared")
    }

    val expenseList get() = tmpExpenseViewModel.tmpExpenseList

    //これリアルタイム同期するのか？ 他端末からCategoryを追加してみて、反映されるかみてみる
    val allCategories: StateFlow<List<Category>> get() = expenseSharedViewModel.allCategories

    private val _splitInputEnabled = MutableStateFlow(false)
    val splitInputEnabled: StateFlow<Boolean> = _splitInputEnabled

    /* 分割入力のときの合計金額 */
    private val _totalAmount = MutableStateFlow<Long>(0)
    val totalAmount: StateFlow<Long> = _totalAmount

    /* 設定のタイムゾーンに合わせて現在日付 */
    fun getTimeZoneDate(): LocalDate {
        /* とりあえず日本で固定 */
        AppTimeZone.isoStringToLocalDateTime(getHeadExpense().datetime)?.let {
            return it.toLocalDate()
        }
        return AppTimeZone.getCurrentTimeInZone().toLocalDate()
    }

    /* 設定のタイムゾーンに合わせた現在時間 */
    fun getTimeZoneTime(): LocalTime {
        AppTimeZone.isoStringToLocalDateTime(getHeadExpense().datetime)?.let {
            return it.toLocalTime()
        }
        return AppTimeZone.getCurrentTimeInZone().toLocalTime()
    }

    fun getSeparatedGeneratedType(): List<String> {
        val buf = getHeadExpense().generatedType
            ?: /* ここに来ることはない */
            return emptyList()
        return separateStringByBars(buf)
    }

    fun getGeneratedTypeDisplay(): String {
        val buf = getHeadExpense().generatedType
            ?: /* ここに来ることはない */
            return "エラー"

        val (mainType, subType) = convertGeneratedTypeToDisplayName(buf)
        if (subType == null) {
            return mainType
        } else {
            return "${mainType}/${subType}"
        }
    }

    fun getHeadExpense(): Expense {
        return expenseList.value.first()
    }

    fun updateTmpExpenseDatetime(datetimeStr: String) {
//        tmpExpenseViewModel.updateTmpExpense(
//            currentTmpExpense.copy(datetime = datetimeStr)
//        )
    }

    // 各項目を個別に更新するメソッド
    fun updateTmpExpenseAmountAt(index: Int = 0, newAmount: Long?) {
        tmpExpenseViewModel.updateTmpExpenseAt(
            index,
            expenseList.value[index].copy(
                amount = newAmount
            )
        )
    }

    fun updateTmpExpenseCategoryAt(index: Int = 0, newCategory: Category?) {
        tmpExpenseViewModel.updateTmpExpenseAt(
            index,
            expenseList.value[index].copy(
                category = newCategory
            )
        )
    }

    fun updateTmpExpenseNoteAt(index: Int = 0, newNote: String) {
        tmpExpenseViewModel.updateTmpExpenseAt(
            index,
            expenseList.value[index].copy(
                note = newNote
            )
        )
    }

    fun updateTmpExpenseItemNameAt(index: Int = 0, itemName: String) {
        tmpExpenseViewModel.updateTmpExpenseAt(
            index,
            expenseList.value[index].copy(
                itemName = itemName
            )
        )
    }

    /**
     * こいつらは共通なのでheadだけ変更して、追加するときに
     * すべて代入する
     */
    fun updateTmpExpenseStoreName(storeName: String) {
        tmpExpenseViewModel.updateTmpExpense(
            getHeadExpense().copy(
                storeName = storeName
            )
        )
    }

    fun resetTmpExpenseList() {
        tmpExpenseViewModel.resetTmpExpenseList()
    }

    fun copyCommonPropertyToExpenses() {}

    fun addTmpExpenseToDb(callback: (SuspendFuncStatusInfo) -> Unit) {
        /**
         * ここで中身チェックを行った方が良い。
         * あくまでViewではこいつを呼び出すだけで。
         */
        var cnt = 0
        viewModelScope.launch {
            for (ex in expenseList.value) {
                val ret = expenseSharedViewModel.addExpense(
                    ex
                )
                if (ret is FuncResultWithData.Success) {
                    cnt++
                }
            }

            if (expenseList.value.size == cnt) {
                callback(
                    SuspendFuncStatusInfo(
                        SuspendFuncStatus.SUCCESS,
                        "追加に成功しました"
                    )
                )
            } else {
                callback(
                    SuspendFuncStatusInfo(
                        status = SuspendFuncStatus.FAILED, errorMessage = "追加に失敗しました"
                    )
                )
            }
        }
    }

    fun updateTmpExpenseToDb(onStart: () -> Unit, callback: (SuspendFuncStatusInfo) -> Unit) {
        onStart()
        var cnt: Int = 0
        viewModelScope.launch {
            for (ex in expenseList.value) {
                if (ex.id == null) {
                    /* 新規作成 */
                    val addRet = expenseSharedViewModel.addExpense(ex)
                    if (addRet is FuncResultWithData.Success) {
                        cnt++
                    }
                } else {
                    /* update */
                    val updateRet = expenseSharedViewModel.updateExpense(ex)
                    if (updateRet.status == SuspendFuncStatus.SUCCESS) {
                        cnt++
                    }
                }
            }

            if (cnt == expenseList.value.size) {
                callback(
                    SuspendFuncStatusInfo(
                        SuspendFuncStatus.SUCCESS,
                        "更新に成功しました"
                    )
                )
            } else {
                callback(
                    SuspendFuncStatusInfo(
                        status = SuspendFuncStatus.FAILED, errorMessage = "更新に失敗しました"
                    )
                )
            }
        }
    }

    fun removeTmpExpenseToDb(onStart: () -> Unit, callback: (SuspendFuncStatusInfo) -> Unit) {
        /**
         * 分割入力のときは、
         * それをオフにしてからにする
         */
        onStart()
        viewModelScope.launch {
            val ret = expenseSharedViewModel.removeExpense(getHeadExpense())
            callback(ret)
        }
    }

    /* カテゴリーを更新する。通信エラーが起きているとカテゴリーが取れていないときがある */
    fun updateStoredCategories(
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        expenseSharedViewModel.clearAllCategories()
        viewModelScope.launch {
            val ret = expenseSharedViewModel.fetchAllCategories()
            val listenerRet = expenseSharedViewModel.addCategoryListeners()
            callback(ret)
        }
    }

    /* ------------------カテゴリー割当を扱う----------------------- */
    fun addCategoryAssignment(
        onStart: () -> Unit = {},
        assignment: CategoryAssignment,
        namePattern: CategoryAssignNamePattern,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ) {
        onStart()
        viewModelScope.launch {
            val ret =
                categoryAssignmentUseCase.addCategoryAssignmentWithCheck(assignment, namePattern)
            callback(ret)
        }
    }
}
