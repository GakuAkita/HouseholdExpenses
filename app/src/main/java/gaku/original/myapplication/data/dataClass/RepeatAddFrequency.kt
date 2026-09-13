package gaku.original.myapplication.data.dataClass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.time.DayOfWeek

@Serializable
@Parcelize
sealed interface RepeatFrequency : Parcelable {
    data class EveryYear(
        val month: Int = 1,
        val day: Int = 1,
        val hour: Int = 0,
        val minute: Int = 0
    ) : RepeatFrequency

    data class EveryMonth(
        val day: Int = 1,
        val hour: Int = 1,
        val minute: Int = 1
    ) : RepeatFrequency

    data class EveryWeek(
        val dayOfWeek: List<DayOfWeek> = emptyList(),
        val hour: Int = 0,
        val minute: Int = 0
    ) : RepeatFrequency

    data class Weekdays(
        val hour: Int = 0,
        val minute: Int = 0
    ) : RepeatFrequency

    data class Weekends(
        val hour: Int = 0,
        val minute: Int = 0
    ) : RepeatFrequency

    data class Everyday(
        val hour: Int = 0,
        val minute: Int = 0
    ) : RepeatFrequency

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
                "frequency" to "every_year",
                "month" to month,
                "day" to day,
                "hour" to hour,
                "minute" to minute
            )
        }

        is RepeatFrequency.EveryMonth -> {
            mapOf(
                "frequency" to "every_month",
                "day" to day,
                "hour" to hour,
                "minute" to minute
            )
        }

        is RepeatFrequency.Everyday -> {
            mapOf(
                "frequency" to "everyday",
                "hour" to hour,
                "minute" to minute
            )
        }

        is RepeatFrequency.Weekdays -> {
            mapOf(
                "frequency" to "weekdays",
                "hour" to hour,
                "minute" to minute
            )
        }

        is RepeatFrequency.Weekends -> {
            mapOf(
                "frequency" to "weekends",
                "hour" to hour,
                "minute" to minute
            )
        }

        is RepeatFrequency.EveryWeek -> {
            mapOf(
                "frequency" to "every_week",
                "dayOfWeek" to dayOfWeek,
                "hour" to hour,
                "minute" to minute
            )
        }
    }
}