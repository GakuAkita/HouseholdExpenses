package gaku.original.myapplication.data.dataClass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import timber.log.Timber

/**
 * kotlin(java) DayOfWeek can't be serialized.
 * I create new class to serialize it.
 */
@Serializable
enum class DayOfWeekSerializable {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}

@Serializable
@Parcelize
sealed interface RepeatFrequency : Parcelable {

    @Serializable
    @Parcelize
    data class EveryYear(
        val month: Int = 1,
        val day: Int = 1,
        val hour: Int = 0,
        val minute: Int = 0
    ) : RepeatFrequency {
        companion object {
            val NAME = "every_year"
        }
    }

    @Serializable
    @Parcelize
    data class EveryMonth(
        val day: Int = 1,
        val hour: Int = 1,
        val minute: Int = 1
    ) : RepeatFrequency {
        companion object {
            val NAME = "every_month"
        }
    }

    @Serializable
    @Parcelize
    data class EveryWeek(
        val dayOfWeek: List<DayOfWeekSerializable> = emptyList(),
        val hour: Int = 0,
        val minute: Int = 0
    ) : RepeatFrequency {
        companion object {
            val NAME = "every_week"
        }
    }

    @Serializable
    @Parcelize
    data class Weekdays(
        val hour: Int = 0,
        val minute: Int = 0
    ) : RepeatFrequency {
        companion object {
            val NAME = "weekdays"
        }
    }

    @Serializable
    @Parcelize
    data class Weekends(
        val hour: Int = 0,
        val minute: Int = 0
    ) : RepeatFrequency {
        companion object {
            val NAME = "weekends"
        }
    }

    @Serializable
    @Parcelize
    data class Everyday(
        val hour: Int = 0,
        val minute: Int = 0
    ) : RepeatFrequency {
        companion object {
            val NAME = "everyday"
        }
    }

    companion object {
        val types = listOf(
            EveryYear(),
            EveryMonth(),
            EveryWeek(),
            Weekdays(),
            Weekends(),
            Everyday()
        )
    }
}

fun RepeatFrequency.withTime(hour: Int, minute: Int): RepeatFrequency {
    return when (this) {
        is RepeatFrequency.EveryYear -> this.copy(hour = hour, minute = minute)
        is RepeatFrequency.EveryMonth -> this.copy(hour = hour, minute = minute)
        is RepeatFrequency.EveryWeek -> this.copy(hour = hour, minute = minute)
        is RepeatFrequency.Weekdays -> this.copy(hour = hour, minute = minute)
        is RepeatFrequency.Weekends -> this.copy(hour = hour, minute = minute)
        is RepeatFrequency.Everyday -> this.copy(hour = hour, minute = minute)
    }
}

fun RepeatFrequency.toFirestore(): Map<String, Any?> {
    return when (this) {
        is RepeatFrequency.EveryYear -> {
            mapOf(
                "frequency" to RepeatFrequency.EveryYear.NAME,
                "month" to month,
                "day" to day,
                "hour" to hour,
                "minute" to minute
            )
        }

        is RepeatFrequency.EveryMonth -> {
            mapOf(
                "frequency" to RepeatFrequency.EveryMonth.NAME,
                "day" to day,
                "hour" to hour,
                "minute" to minute
            )
        }

        is RepeatFrequency.Everyday -> {
            mapOf(
                "frequency" to RepeatFrequency.Everyday.NAME,
                "hour" to hour,
                "minute" to minute
            )
        }

        is RepeatFrequency.Weekdays -> {
            mapOf(
                "frequency" to RepeatFrequency.Weekdays.NAME,
                "hour" to hour,
                "minute" to minute
            )
        }

        is RepeatFrequency.Weekends -> {
            mapOf(
                "frequency" to RepeatFrequency.Weekends.NAME,
                "hour" to hour,
                "minute" to minute
            )
        }

        is RepeatFrequency.EveryWeek -> {
            mapOf(
                "frequency" to RepeatFrequency.EveryWeek.NAME,
                "dayOfWeek" to dayOfWeek,
                "hour" to hour,
                "minute" to minute
            )
        }
    }
}

fun Map<String, Any?>.toRepeatFrequency(): RepeatFrequency {
    Timber.d("Converting :${this}")

    val hour = (get("hour") as? Number)?.toInt()
    val minute = (get("minute") as? Number)?.toInt()

    return when (get("frequency")) {
        RepeatFrequency.EveryYear.NAME -> {
            Timber.d("Converting :${this} ${get("month")}")

            RepeatFrequency.EveryYear(
                month = (get("month") as? Number)?.toInt() ?: error("month is null"),
                day = (get("day") as? Number)?.toInt() ?: error("day is null"),
                hour = hour ?: error("hour is null"),
                minute = minute ?: error("minute is null")
            )
        }

        RepeatFrequency.EveryMonth.NAME -> {
            RepeatFrequency.EveryMonth(
                day = (get("day") as? Number)?.toInt() ?: error("day is null"),
                hour = hour ?: error("hour is null"),
                minute = minute ?: error("minute is null")
            )
        }

        RepeatFrequency.EveryWeek.NAME -> {
            RepeatFrequency.EveryWeek(
                dayOfWeek = get("dayOfWeek") as? List<DayOfWeekSerializable>
                    ?: error("dayOfWeek is null"),
                hour = hour ?: error("hour is null"),
                minute = minute ?: error("minute is null")
            )
        }

        RepeatFrequency.Weekends.NAME -> {
            RepeatFrequency.Weekends(
                hour = hour ?: error("hour is null"),
                minute = minute ?: error("minute is null")
            )
        }

        RepeatFrequency.Weekdays.NAME -> {
            RepeatFrequency.Weekdays(
                hour = hour ?: error("hour is null"),
                minute = minute ?: error("minute is null")
            )
        }

        RepeatFrequency.Everyday.NAME -> {
            RepeatFrequency.Everyday(
                hour = hour ?: error("hour is null"),
                minute = minute ?: error("minute is null")
            )
        }

        else -> {
            throw Exception("Unknown RepeatFrequency: ${get("frequencyInfo")}")
        }
    }
}