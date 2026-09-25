package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.executeDialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.common.CodingErrorException
import gaku.original.myapplication.data.dataClass.GeneratedType
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.dataClass.RepeatFrequency
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

data class RepeatAddExecuteUiState(
    val inProgressPercent: Double = 0.0,
    val isWorking: Boolean = false,
    val isDone: Boolean = false,

    val message: String? = null,

    val amount: Long = 0L,
    val categoryName: String = "",
    val frequency: RepeatFrequency = RepeatFrequency.EveryYear()
)

class RepeatAddExecuteViewModel(
    private val repeatAdd: RepeatAdd,
    private val appTimeZoneRepository: AppTimeZoneRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    private val zoneId = appTimeZoneRepository.zoneId.value

    private val _uiState = MutableStateFlow(RepeatAddExecuteUiState())
    val uiState: StateFlow<RepeatAddExecuteUiState> = _uiState.asStateFlow()

    companion object {
        fun Factory(repeatAdd: RepeatAdd): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                RepeatAddExecuteViewModel(
                    repeatAdd, session.appTimeZoneRepository, session.expenseRepository
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")

        _uiState.update {
            it.copy(
                amount = repeatAdd.expense.amount!!,
                categoryName = repeatAdd.expense.category?.name!!,
                frequency = repeatAdd.frequencyInfo!!
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

    fun onYesClick() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isWorking = true, inProgressPercent = 0.0
                    )
                }
                val now = Instant.now().atZone(zoneId)
                Timber.d("${now.year} / ${now.monthValue} ")
                val yearMonth = YearMonth.of(now.year, now.monthValue)
                val targets = repeatAdd.frequencyInfo?.getRepeatAddTargetDaysOfMonth(
                    yearMonth, zoneId
                )

                if (targets == null) {
                    throw CodingErrorException("targets is null")
                }

                if (targets.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            inProgressPercent = 100.0
                        )
                    }
                } else {
                    var progress = 0.0
                    for (target in targets) {
                        Timber.d("target:$target now:${Instant.now()}")
                        if (target.isAfter(Instant.now())) {
                            val expense = repeatAdd.expense.copy(
                                datetime = target.toString(),
                                generatedType = GeneratedType.RepeatAdd(
                                    repeatAdd.id!!
                                )
                            )
                            expenseRepository.addExpense(expense)
                        }
                        progress += 100.0 / targets.size
                        _uiState.update {
                            it.copy(
                                inProgressPercent = progress
                            )
                        }
                    }
                }
                /* I didn't think about when the user suspended and wanted to resume. */
                /* Implement it if needed. */
                _uiState.update {
                    it.copy(
                        isDone = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isWorking = false, message = e.message
                    )
                }
            }
        }
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}

/* return target dates for the repeatAdd */
fun RepeatFrequency.getRepeatAddTargetDaysOfMonth(
    yearMonth: YearMonth, /* This is  */
    timeZone: ZoneId
): List<Instant> {
    when (this) {
        is RepeatFrequency.EveryYear -> {
            if (yearMonth.monthValue != this.month) {
                return emptyList()
            } else {
                return listOf(
                    LocalDateTime.of(
                        yearMonth.year, yearMonth.month, this.day, this.hour, this.minute, 0
                    ).atZone(timeZone).toInstant()
                )
            }
        }

        is RepeatFrequency.EveryMonth -> {
            return listOf(
                LocalDateTime.of(
                    yearMonth.year, yearMonth.month, this.day, this.hour, this.minute, 0
                ).atZone(timeZone).toInstant()
            )
        }

        is RepeatFrequency.EveryWeek -> {
            var targets = emptyList<Instant>()
            val daysList = this.dayOfWeek
            for (i in 1..yearMonth.lengthOfMonth()) {
                val date = yearMonth.atDay(i)
                if (date.dayOfWeek in daysList) {
                    targets += date.atTime(this.hour, this.minute).atZone(timeZone).toInstant()
                }
            }

            return targets
        }

        is RepeatFrequency.Weekdays -> {
            var targets = emptyList<Instant>()
            val daysList = listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            )
            for (i in 1..yearMonth.lengthOfMonth()) {
                val date = yearMonth.atDay(i)
                if (date.dayOfWeek in daysList) {
                    targets += date.atTime(this.hour, this.minute).atZone(timeZone).toInstant()
                }
            }
            return targets
        }

        is RepeatFrequency.Weekends -> {
            var targets = emptyList<Instant>()
            val daysList = listOf(
                DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            )
            for (i in 1..yearMonth.lengthOfMonth()) {
                val date = yearMonth.atDay(i)
                if (date.dayOfWeek in daysList) {
                    targets += date.atTime(this.hour, this.minute).atZone(timeZone).toInstant()
                }
            }
            return targets
        }

        is RepeatFrequency.Everyday -> {
            var targets = emptyList<Instant>()
            for (i in 1..yearMonth.lengthOfMonth()) {
                val date = yearMonth.atDay(i)
                targets += date.atTime(this.hour, this.minute).atZone(timeZone).toInstant()
            }
            return targets
        }
    }
}