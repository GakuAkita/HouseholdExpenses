package gaku.original.myapplication.data.repository.repeatAdd

import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.dataClass.RepeatFrequency
import gaku.original.myapplication.data.repository.expense.ExpenseDto
import gaku.original.myapplication.data.repository.expense.toDto
import java.time.DayOfWeek

/**
 * The shape of a RepeatAdd as stored in a database. Only the repositories use this class.
 *
 * Properties are nullable vars with defaults so that Firestore toObject() or Realtime DB getValue() can create it.
 * Keep the property names in sync with Firestore functions (functions/src/type/RepeatAdd.ts).
 */
data class RepeatAddDto(
    var id: String? = null,
    var timestamp: Long? = null,
    var expense: ExpenseDto? = null,
    var frequencyInfo: RepeatFrequencyDto? = null,
) {
    fun toDomain(): RepeatAdd = RepeatAdd(
        id = id ?: error("id is null"),
        timestamp = timestamp ?: error("timestamp is null"),
        expense = expense?.toExpenseTemplate() ?: error("expense is null"),
        frequencyInfo = frequencyInfo?.toDomain() ?: error("frequencyInfo is null"),
    )
}

/** All frequencies share one flat shape. Properties a frequency doesn't use are null. */
data class RepeatFrequencyDto(
    var frequency: String? = null,
    var month: Int? = null,
    var day: Int? = null,
    /* DayOfWeek.name, e.g. "MONDAY" */
    var dayOfWeek: List<String>? = null,
    var hour: Int? = null,
    var minute: Int? = null,
) {
    fun toDomain(): RepeatFrequency {
        val hour = hour ?: error("hour is null")
        val minute = minute ?: error("minute is null")
        return when (frequency) {
            RepeatFrequency.EveryYear.NAME -> RepeatFrequency.EveryYear(
                month = month ?: error("month is null"),
                day = day ?: error("day is null"),
                hour = hour,
                minute = minute
            )

            RepeatFrequency.EveryMonth.NAME -> RepeatFrequency.EveryMonth(
                day = day ?: error("day is null"),
                hour = hour,
                minute = minute
            )

            RepeatFrequency.EveryWeek.NAME -> RepeatFrequency.EveryWeek(
                dayOfWeek = dayOfWeek?.map { DayOfWeek.valueOf(it) } ?: error("dayOfWeek is null"),
                hour = hour,
                minute = minute
            )

            RepeatFrequency.Weekdays.NAME -> RepeatFrequency.Weekdays(hour = hour, minute = minute)
            RepeatFrequency.Weekends.NAME -> RepeatFrequency.Weekends(hour = hour, minute = minute)
            RepeatFrequency.Everyday.NAME -> RepeatFrequency.Everyday(hour = hour, minute = minute)
            else -> error("Unknown RepeatFrequency: $frequency")
        }
    }
}

fun RepeatAdd.toDto(): RepeatAddDto = RepeatAddDto(
    id = id,
    timestamp = timestamp,
    expense = expense.toDto(),
    frequencyInfo = frequencyInfo.toDto(),
)

fun RepeatFrequency.toDto(): RepeatFrequencyDto = when (this) {
    is RepeatFrequency.EveryYear -> RepeatFrequencyDto(
        frequency = RepeatFrequency.EveryYear.NAME,
        month = month,
        day = day,
        hour = hour,
        minute = minute
    )

    is RepeatFrequency.EveryMonth -> RepeatFrequencyDto(
        frequency = RepeatFrequency.EveryMonth.NAME,
        day = day,
        hour = hour,
        minute = minute
    )

    is RepeatFrequency.EveryWeek -> RepeatFrequencyDto(
        frequency = RepeatFrequency.EveryWeek.NAME,
        dayOfWeek = dayOfWeek.map { it.name },
        hour = hour,
        minute = minute
    )

    is RepeatFrequency.Weekdays -> RepeatFrequencyDto(
        frequency = RepeatFrequency.Weekdays.NAME,
        hour = hour,
        minute = minute
    )

    is RepeatFrequency.Weekends -> RepeatFrequencyDto(
        frequency = RepeatFrequency.Weekends.NAME,
        hour = hour,
        minute = minute
    )

    is RepeatFrequency.Everyday -> RepeatFrequencyDto(
        frequency = RepeatFrequency.Everyday.NAME,
        hour = hour,
        minute = minute
    )
}
