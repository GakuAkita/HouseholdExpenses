package gaku.original.myapplication.ui.screens.global.expenseAddEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.common.AppError
import gaku.original.myapplication.common.AppResult
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.appTimeZone.toInstant
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import gaku.original.myapplication.utility.roundToLongOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class ExpenseAddEditUiState(
    val isEdit: Boolean = false,
    val message: String? = null,
    val isLoading: Boolean = false,

    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime? = null,
    /* Always a list. A single expense is a list of one item. */
    val expenseEditList: List<ExpenseEditItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val totalAmount: Long = 0L,
    /* Which amount the calculator is editing. null when the calculator is hidden. */
    val calculatorTarget: CalculatorTarget? = null,
    val isDatePickerVisible: Boolean = false,
    val isTimePickerVisible: Boolean = false,
    val isSplitInputEnabled: Boolean = false,

    val placeName: String = "",

    val isSaveDone: Boolean = false,
    val isDeleteDone: Boolean = false,
) {
    val calculatorInitialValue: Long
        get() = when (calculatorTarget) {
            CalculatorTarget.TotalAmount -> totalAmount
            is CalculatorTarget.Item -> expenseEditList[calculatorTarget.index].amount ?: 0L
            null -> 0L
        }
}

sealed interface CalculatorTarget {
    data object TotalAmount : CalculatorTarget
    data class Item(val index: Int) : CalculatorTarget
}

sealed interface ExpenseInputError : AppError {
    data object DateEmpty : ExpenseInputError {
        override val message: String
            get() = "Date is empty"
    }

    data object TimeEmpty : ExpenseInputError {
        override val message: String
            get() = "Time is empty"
    }

    data object AmountNegative : ExpenseInputError {
        override val message: String
            get() = "Amount is negative"
    }

    data object AmountEmpty : ExpenseInputError {
        override val message: String
            get() = "Amount is empty"
    }

    data object CategoryEmpty : ExpenseInputError {
        override val message: String
            get() = "Category is empty"
    }

    data class TotalExpenseNotMatch(val amountInput: Long, val amountCalc: Long) :
        ExpenseInputError {
        override val message: String
            get() = "Total amount not match. Input:$amountInput, Calc:$amountCalc"
    }
}

fun ExpenseAddEditUiState.validate(): AppResult<Unit, ExpenseInputError> {
    if (selectedDate == null) {
        return AppResult.Failure(ExpenseInputError.DateEmpty)
    }
    if (selectedTime == null) {
        return AppResult.Failure(ExpenseInputError.TimeEmpty)
    }

    for (item in expenseEditList) {
        if (item.amount == null) {
            return AppResult.Failure(ExpenseInputError.AmountEmpty)
        }
        if (item.amount < 0) {
            return AppResult.Failure(ExpenseInputError.AmountNegative)
        }
        if (item.category == null) {
            return AppResult.Failure(ExpenseInputError.CategoryEmpty)
        }
    }

    if (expenseEditList.size > 1) {
        val totalAmountCalc = expenseEditList.sumOf { it.amount ?: 0L }
        if (totalAmountCalc != totalAmount) {
            return AppResult.Failure(
                ExpenseInputError.TotalExpenseNotMatch(totalAmount, totalAmountCalc)
            )
        }
    }

    return AppResult.Success(Unit)
}

fun initialUiState(mode: ExpenseAddEditMode, zoneId: ZoneId): ExpenseAddEditUiState =
    when (mode) {
        is ExpenseAddEditMode.Edit -> {
            val expense = mode.expense
            val localDateTime = expense.datetime.atZone(zoneId).toLocalDateTime()
            ExpenseAddEditUiState(
                isEdit = true,
                expenseEditList = listOf(
                    ExpenseEditItem(
                        original = expense,
                        amount = expense.amount,
                        category = expense.category,
                        note = expense.note,
                        productName = expense.itemName
                    )
                ),
                placeName = expense.storeName ?: "",
                selectedDate = localDateTime.toLocalDate(),
                selectedTime = localDateTime.toLocalTime(),
            )
        }

        is ExpenseAddEditMode.New -> {
            val prefill = mode.prefill
            /* based on the selected timezone, decide initial Date and Time */
            val localDateTime = prefill.datetime?.atZone(zoneId)?.toLocalDateTime()
            ExpenseAddEditUiState(
                isEdit = false,
                expenseEditList = listOf(ExpenseEditItem(amount = prefill.amount)),
                placeName = prefill.storeName ?: "",
                selectedDate = localDateTime?.toLocalDate() ?: LocalDate.now(zoneId),
                selectedTime = localDateTime?.toLocalTime() ?: LocalTime.now(zoneId),
            )
        }
    }

class ExpenseAddEditViewModel(
    private val mode: ExpenseAddEditMode,
    private val expenseRepository: ExpenseRepository,
    private val appTimeZoneRepository: AppTimeZoneRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    val zoneId: ZoneId = appTimeZoneRepository.zoneId.value

    /* Common to all the items. Items added while editing take the edited expense's type. */
    private val generatedType = when (mode) {
        is ExpenseAddEditMode.Edit -> mode.expense.generatedType
        is ExpenseAddEditMode.New -> mode.prefill.generatedType
    }

    companion object {
        fun Factory(mode: ExpenseAddEditMode): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                val expenseRepository = app.appContainer.sessionContainer!!.expenseRepository
                val appTimeZoneRepository =
                    app.appContainer.sessionContainer!!.appTimeZoneRepository
                val categoryRepository = app.appContainer.sessionContainer!!.categoryRepository
                ExpenseAddEditViewModel(
                    mode,
                    expenseRepository,
                    appTimeZoneRepository,
                    categoryRepository
                )
            }
        }
    }

    private val _uiState = MutableStateFlow(initialUiState(mode, zoneId))
    val uiState get() = _uiState.asStateFlow()

    init {
        Timber.d("Created. ${hashCode()}")

        viewModelScope.launch {
            try {
                categoryRepository.categories.collect { categories ->
                    _uiState.update { it.copy(categories = categories.values.toList()) }
                }
            } catch (e: Exception) {
                showMessage("Failed to fetch categories.:${e.message}")
            }
        }
    }

    private fun showMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }

    private fun updateItem(index: Int, transform: (ExpenseEditItem) -> ExpenseEditItem) {
        _uiState.update { it.copy(expenseEditList = it.expenseEditList.updateAt(index, transform)) }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    /*************************************************/
    /* Date and Time */
    /*************************************************/
    fun onDateFieldClick() {
        if (_uiState.value.isTimePickerVisible) {
            return
        }
        _uiState.update { it.copy(isDatePickerVisible = true) }
    }

    fun onDateSelected(dateMillis: Long?) {
        if (dateMillis == null) {
            return
        }

        /* first accept it as local date in the selected timezone */
        /* when save it to the database, converted to UTC */
        val localDate = Instant.ofEpochMilli(dateMillis).atZone(zoneId).toLocalDate()
        _uiState.update { it.copy(selectedDate = localDate, isDatePickerVisible = false) }
    }

    fun onDatePickerDismiss() {
        _uiState.update { it.copy(isDatePickerVisible = false) }
    }

    fun onTimeFieldClick() {
        if (_uiState.value.isDatePickerVisible) {
            return
        }
        _uiState.update { it.copy(isTimePickerVisible = true) }
    }

    fun onTimeSelected(time: LocalTime) {
        /* This time is current timezone. */
        _uiState.update { it.copy(selectedTime = time, isTimePickerVisible = false) }
    }

    fun onTimePickerDismiss() {
        _uiState.update { it.copy(isTimePickerVisible = false) }
    }

    /*************************************************/
    /* Split input */
    /*************************************************/
    fun onSwitchClick() {
        val state = _uiState.value
        if (state.isSplitInputEnabled) {
            /* switched to off. Only head element remains */
            _uiState.update {
                it.copy(
                    isSplitInputEnabled = false,
                    expenseEditList = it.expenseEditList.take(1)
                )
            }
            return
        }

        /* switched to on */
        val headAmount = state.expenseEditList.first().amount
        if (headAmount == null || headAmount == 0L) {
            /* any value should be input beforehand */
            showMessage("Unable to turn on split input. Please input the amount first.")
            return
        }
        if (state.expenseEditList.size != 1) {
            showMessage("Expense should be always only one. Something wrong. Please contact the developer")
            return
        }
        _uiState.update {
            it.copy(
                isSplitInputEnabled = true,
                totalAmount = headAmount,
                expenseEditList = it.expenseEditList + ExpenseEditItem(amount = 0)
            )
        }
    }

    fun onAddClick() {
        val lastAmount = _uiState.value.expenseEditList.last().amount
        if (lastAmount == null || lastAmount == 0L) {
            showMessage("Last Expense amount should not be empty")
            return
        }
        _uiState.update { it.copy(expenseEditList = it.expenseEditList + ExpenseEditItem(amount = 0)) }
    }

    /*************************************************/
    /* Calculator */
    /*************************************************/
    fun onTotalAmountClick() {
        _uiState.update { it.copy(calculatorTarget = CalculatorTarget.TotalAmount) }
    }

    fun onAmountClick(index: Int) {
        _uiState.update { it.copy(calculatorTarget = CalculatorTarget.Item(index)) }
    }

    fun onCalculatorDecide(value: String) {
        if (value.isEmpty()) {
            showMessage("Value is empty.")
            return
        }
        val amount = value.roundToLongOrNull()
        if (amount == null) {
            showMessage("Unable to read the value. It might be too big.")
            return
        }

        _uiState.update {
            var state = when (val target = it.calculatorTarget) {
                CalculatorTarget.TotalAmount -> it.copy(totalAmount = amount)
                is CalculatorTarget.Item -> it.copy(
                    expenseEditList = it.expenseEditList.updateAt(target.index) { item ->
                        item.copy(amount = amount)
                    }
                )

                null -> it
            }
            if (state.isSplitInputEnabled) {
                state = state.copy(
                    expenseEditList = state.expenseEditList.withRemainingOnLast(state.totalAmount)
                )
            }
            state.copy(calculatorTarget = null)
        }
    }

    fun onCalculatorDismiss() {
        _uiState.update { it.copy(calculatorTarget = null) }
    }

    /*************************************************/
    /* Item fields */
    /*************************************************/
    fun onCategorySelected(index: Int, category: Category?) {
        updateItem(index) { it.copy(category = category) }
    }

    fun onCategoryRefreshClick() {
        showMessage("Not implemented yet.")
    }

    fun onNoteChange(index: Int, note: String) {
        updateItem(index) { it.copy(note = note) }
    }

    fun onProductNameChange(index: Int, productName: String) {
        updateItem(index) { it.copy(productName = productName) }
    }

    fun onPlaceNameChange(placeName: String?) {
        _uiState.update { it.copy(placeName = placeName ?: "") }
    }

    /*************************************************/
    /* Save and Delete */
    /*************************************************/
    fun onSaveClick() {
        val state = _uiState.value
        val validate = state.validate()
        if (validate is AppResult.Failure) {
            showMessage(validate.error.message)
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val datetime = state.selectedDate!!.atTime(state.selectedTime!!).toInstant(zoneId)
                val now = System.currentTimeMillis()
                for (item in state.expenseEditList) {
                    val expense = item.toExpense(datetime, state.placeName, generatedType, now)
                    Timber.d("Expense generated:$expense")
                    if (item.original != null) {
                        expenseRepository.updateExpense(expense)
                    } else {
                        expenseRepository.addExpense(expense)
                    }
                }
                _uiState.update { it.copy(isSaveDone = true, message = "Expense Saved") }
            } catch (e: Exception) {
                Timber.d("Catched error:${e.message}")
                _uiState.update { it.copy(isLoading = false, message = e.message) }
            }
        }
    }

    fun onDeleteClick() {
        val id = (mode as? ExpenseAddEditMode.Edit)?.expense?.id
        if (id == null) {
            showMessage("Coding Error: Delete should not exist when add.")
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                expenseRepository.removeExpense(id)
                _uiState.update { it.copy(isDeleteDone = true, message = "Expense Deleted") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, message = e.message) }
            }
        }
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}
