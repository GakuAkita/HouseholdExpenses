package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.editDialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.common.AppError
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.dataClass.RepeatFrequency
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.repeatAdd.RepeatAddRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek

data class RepeatAddEditDialogState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val amount: Long? = null,
    val note: String? = null,
    val itemName: String? = null,
    val storeName: String? = null,
    val category: Category? = null,
    val frequency: RepeatFrequency? = null,
    val categories: List<Category> = emptyList(),

    val month: Int? = null,
    val day: Int? = null,
    val dayOfWeek: List<DayOfWeek> = emptyList(),
    val hour: Int? = null,
    val minute: Int? = null
)

data class RepeatAddError : AppError {

}

class RepeatAddEditViewModel(
    private val initialRepeatAdd: RepeatAdd? = null,
    private val repeatAddRepository: RepeatAddRepository,
    private val categoryRepository: CategoryRepository,
    private val appTimeZoneRepository: AppTimeZoneRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepeatAddEditDialogState())
    val uiState: StateFlow<RepeatAddEditDialogState> = _uiState.asStateFlow()

    companion object {
        fun Factory(repeatAdd: RepeatAdd?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                RepeatAddEditViewModel(
                    repeatAdd,
                    session.repeatAddRepository,
                    session.categoryRepository,
                    session.appTimeZoneRepository
                )
            }
        }
    }


    init {
        Timber.d("Created. ${hashCode()}")

        viewModelScope.launch {
            categoryRepository.categories.collect { categories ->
                _uiState.update {
                    it.copy(
                        categories = categories.values.toList()
                    )
                }
            }
        }

        if (initialRepeatAdd == null) {
            //新規追加
            _uiState.update {
                it.copy(
                    amount = null,
                    note = null,
                    itemName = null,
                    storeName = null,
                    category = null,
                    frequency = null
                )
            }
        } else {
            //編集
            _uiState.update {
                it.copy(
                    amount = initialRepeatAdd.expense.amount,
                    note = initialRepeatAdd.expense.note,
                    itemName = initialRepeatAdd.expense.itemName,
                    storeName = initialRepeatAdd.expense.storeName,
                )
            }
        }
    }

    fun onAmountChange(text: String) {
        if (text.isEmpty()) {
            _uiState.update {
                it.copy(
                    amount = null
                )
            }
        } else {
            val amount = text.toLongOrNull()
            if (amount != null) {
                _uiState.update {
                    it.copy(
                        amount = text.toLongOrNull()
                    )
                }
            }
        }
    }

    fun onCategorySelected(category: Category?) {
        _uiState.update {
            it.copy(
                category = category
            )
        }
    }

    fun onNoteChange(note: String?) {
        _uiState.update {
            it.copy(
                note = note
            )
        }
    }

    fun onItemNameChange(itemName: String?) {
        _uiState.update {
            it.copy(
                itemName = itemName
            )
        }
    }

    fun onStoreNameChange(storeName: String?) {
        _uiState.update {
            it.copy(
                storeName = storeName
            )
        }
    }

    fun onRepeatFrequencySelected(
        freq: RepeatFrequency
    ) {
        _uiState.update {
            it.copy(
                frequency = freq
            )
        }
    }

    fun onMonthChange(month: String) {
        if (month.isEmpty()) {
            _uiState.update {
                it.copy(
                    month = null
                )
            }
            return
        }

        val monthInt = month.toIntOrNull()
        if (monthInt == null) {
            _uiState.update {
                it.copy(
                    message = "Bug: Unable to convert to Integer"
                )
            }
            return
        } else if (monthInt !in 1..12) {
            return
        } else {
            /* if the day is not appropriate, automatically adjust it. */
            _uiState.update {
                it.copy(
                    month = monthInt
                )
            }
        }
    }

    fun onDayChange(day: String) {
        if (day.isEmpty()) {
            _uiState.update {
                it.copy(
                    day = null
                )
            }
            return
        }

        val dayInt = day.toIntOrNull()
        if (dayInt == null) {
            _uiState.update {
                it.copy(
                    message = "Bug: Unable to convert to Integer"
                )
            }
            return
        } else if (dayInt !in 1..31) {
            return
        } else {
            _uiState.update {
                it.copy(
                    day = dayInt
                )
            }
        }
    }

    fun onHourChange(hour: String) {
        if (hour.isEmpty()) {
            _uiState.update {
                it.copy(
                    hour = null
                )
            }
            return
        }

        val hourInt = hour.toIntOrNull()
        if (hourInt == null) {
            _uiState.update {
                it.copy(
                    message = "Bug: Unable to convert to Integer"
                )
            }
            return
        } else if (hourInt !in 0..23) {
//            _uiState.update {
//                it.copy(
//                    message = "Bug: Hour must be between 0 and 23"
//                )
//            }
            /* Out of range */
            return
        } else {
            _uiState.update {
                it.copy(
                    hour = hourInt
                )
            }
        }
    }

    fun onMinuteChange(minute: String) {
        if (minute.isEmpty()) {
            _uiState.update {
                it.copy(
                    minute = null
                )
            }
            return
        }

        val minuteInt = minute.toIntOrNull()
        if (minuteInt == null) {
            _uiState.update {
                it.copy(
                    message = "Bug: Unable to convert to integer"
                )
            }
            return
        } else if (minuteInt !in 0..59) {
            /* Out of range */
            return
        } else {
            _uiState.update {
                it.copy(
                    minute = minuteInt
                )
            }
        }
    }

    fun onSaveClick() {
        _uiState.update {
            it.copy(
                message = "Saved!"
            )
        }
    }

    fun onMessageShown() {
        _uiState.update {
            it.copy(
                message = null
            )
        }
    }

    fun onDayOfWeekChange(dayOfWeek: DayOfWeek, status: Boolean) {
        val current = _uiState.value.dayOfWeek
        if (status) {
            _uiState.update {
                it.copy(
                    dayOfWeek = current.plus(dayOfWeek)
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    dayOfWeek = current.minus(dayOfWeek)
                )
            }
        }
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}