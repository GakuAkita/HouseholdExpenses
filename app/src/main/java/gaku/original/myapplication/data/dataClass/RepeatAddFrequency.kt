package gaku.original.myapplication.data.dataClass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.time.DayOfWeek

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
        val dayOfWeek: List<DayOfWeek> = listOf(),
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
